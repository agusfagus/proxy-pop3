package proxy;

/**
 * Valid POP3 Commands
 */
public enum Command {
    USER, PASS, LIST, RETR, QUIT, UIDL, DELE, STAT, NOOP, RSET, APOP, TOP, UNKNOWN, LIST_MULTI, UIDL_MULTI
}
