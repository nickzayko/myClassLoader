package com.andersen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MyClassLoader extends ClassLoader {
    private HashMap<String, Class<?>> cash = new HashMap<String, Class<?>>();
    private String jarFileName;
    private String packageName;
    private static String WARNING = "Warning : No jar file found. Packet unmarshalling won't be possible. Please verify your classpath";

    public MyClassLoader(String jarFileName, String packageName) {
        this.jarFileName = jarFileName;
        this.packageName = packageName;

        cashClasses();
    }

    //извлекаем все классы из .jar и записываем в кеш
    private void cashClasses() {
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                // Одно из назначений хорошего загрузчика - валидация классов на этапе загрузки
                if (match(normalize(jarEntry.getName()), packageName)) {
                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        Class<?> clazz = defineClass(stripClassName(normalize(jarEntry.getName())), classData, 0, classData.length);
                        cash.put(clazz.getName(), clazz);
                        System.out.println("**** class -- " + clazz.getName() + " loaded in cash! ****");
                    }
                }
            }
        } catch (IOException e) {
            // Просто выведем сообщение об ошибке
            System.out.println(WARNING);
        }
    }


    //    Преобразуем имя в файловой системе в имя класса(заменяем слэши на точки)
    private String normalize(String className) {
        return className.replace("/", ".");
    }

    //   Валидация класса - проверят принадлежит ли класс заданному пакету и имеет ли он расширение .class
    private boolean match(String className, String packageName) {
        return className.startsWith(packageName) && className.startsWith(".class");
    }

    //    Получаем каноническое имя класса
    private String stripClassName(String className) {
        return className.substring(0, className.length() - 6);
    }


    //    Извлекаем файл из заданного JarEntry
    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();
        if (size == -1 || size == 0) {
            return null;
        }
        byte[] data = new byte[(int) size];
        InputStream inputStream = jarFile.getInputStream(jarEntry);
        inputStream.read(data);
        return data;
    }



    //    собственно метод, который загружает сам класс
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = cash.get(name);

        //  возможно класс вызывается не по полному имени - добавим имя пакета
        if (result == null){
            result = cash.get(packageName+"."+name);
        }

        //  если класса нет в кеше, то возможно он системный
        if (result == null){
            result = super.findSystemClass(name);
            System.out.println("*** load class ("+name+")");
        }
        return result;
    }
}
