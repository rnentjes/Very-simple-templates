package nl.astraeus.template;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 4:12 PM
 */
public class ParseException extends RuntimeException {

    private String message;
    private String template;
    private int line;

    public ParseException(String message, String template, int line) {
        super();
        this.message = message;
        this.line = line;
        this.template = template;
    }

    public ParseException(String message, int line) {
        super();
        this.message = message;
        this.line = line;
    }

    @Override
    public String getMessage() {
        if (template != null) {
            return "ParseException '" + message + "' in "+template+":"+line;
        } else {
            return "ParseException '" + message + "' in line: "+line;
        }
    }

}
