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
    private static class ManifestableURLClassLoader extends URLClassLoader {
        ManifestableURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        Manifest loadManifest() throws Exception {
            URL resource = findResource("META-INF/MANIFEST.MF");
            System.out.println(resource);
            if(resource == null) return null;
            InputStream in = resource.openStream();
            try {
                return new Manifest(in);
            } finally {
                in.close();
            }
        }
    }

    private static Method findMethod(String jar) throws Exception {
        File file = new File(jar);
        URL url = file.toURI().toURL();
        ManifestableURLClassLoader cl = new ManifestableURLClassLoader(new URL[] { url }, null);
        Manifest mf = cl.loadManifest();
        if(mf == null) throw new Exception("No manifest defined in " + jar);
        Attributes attribs = mf.getMainAttributes();
        String mainClass = attribs.getValue(Attributes.Name.MAIN_CLASS);
        if(mainClass == null) throw new Exception("No Main-Class defined in " + jar + "'s manifest");
        Class<?> cls = cl.loadClass(mainClass);
        return cls.getMethod("main", String[].class);
    }

    public static void main(String[] args) throws Throwable {
        if(args.length == 0) {
            System.err.println("Usage: java -jar launchjar.jar JARFILE ARG...");
            System.exit(1);
        }
        try {
            findMethod(args[0]).invoke(null, new Object[] { Arrays.copyOfRange(args, 1, args.length) });
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
