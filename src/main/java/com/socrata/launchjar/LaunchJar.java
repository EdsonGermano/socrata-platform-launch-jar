package com.socrata.launchjar;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LaunchJar {
    private static class JARClassLoader extends URLClassLoader {
        private File jar;

        JARClassLoader(File jar, ClassLoader parent) throws Exception {
            super(new URL[] { jar.toURI().toURL() }, parent);
            this.jar = jar;
        }

        File getJar() {
            return jar;
        }

        Manifest loadManifest() throws Exception {
            URL resource = findResource("META-INF/MANIFEST.MF");
            if(resource == null) return null;
            InputStream in = resource.openStream();
            try {
                return new Manifest(in);
            } finally {
                in.close();
            }
        }
    }

    private static Method findMethod(JARClassLoader jarCL) throws Exception {
        Manifest mf = jarCL.loadManifest();
        if(mf == null) throw new Exception("No manifest defined in " + jarCL.getJar());
        Attributes attribs = mf.getMainAttributes();
        String mainClass = attribs.getValue(Attributes.Name.MAIN_CLASS);
        if(mainClass == null) throw new Exception("No Main-Class defined in " + jarCL.getJar() + "'s manifest");
        Class<?> cls = jarCL.loadClass(mainClass);
        return cls.getMethod("main", String[].class);
    }

    public static void main(String[] args) throws Throwable {
        if(args.length == 0) {
            System.err.println("Usage: java -jar launchjar.jar JARFILE ARG...");
            System.exit(1);
        }

        JARClassLoader cl = new JARClassLoader(new File(args[0]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(cl);
        Method method = findMethod(cl);
        try {
            method.invoke(null, new Object[] { Arrays.copyOfRange(args, 1, args.length) });
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
