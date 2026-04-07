package com.example.product_test.config;

public class DbContextHolder {

    private static final ThreadLocal<DbRole> CONTEXT = new ThreadLocal<>();

    private DbContextHolder() {
    }

    public static void useWrite() {
        CONTEXT.set(DbRole.WRITE);
    }

    public static void useRead() {
        CONTEXT.set(DbRole.READ);
    }

    public static DbRole getCurrentRole() {
        DbRole role = CONTEXT.get();
        return role == null ? DbRole.WRITE : role;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
