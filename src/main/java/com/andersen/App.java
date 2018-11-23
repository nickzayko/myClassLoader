package com.andersen;

/**
 *
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException {
        MyClassLoader classLoader = new MyClassLoader("HelloNic.jar", "com.andersen");
        classLoader.loadClass("HelloNic.jar");
    }
}
