package recruitment

class AuditUserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>()
    private static final ThreadLocal<String> currentIP = new ThreadLocal<>()
    private static final ThreadLocal<Long> currentOrg = new ThreadLocal<>()

    static void setCurrentUser(String user) {
        currentUser.set(user);
    }
    static void setOrgid(Long org) {
        currentOrg.set(org);
    }

    static String getCurrentUser() {
        return currentUser.get();
    }

    static Long getCurrentOrg() {
        return currentOrg.get();
    }

    static void clear() {
        currentUser.remove();
        currentIP.remove();
    }

    static void setIP(String ip) {
        currentIP.set(ip);
    }

    static String getIP() {
        return currentIP.get();
    }
}
