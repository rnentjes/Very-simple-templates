package nl.astraeus.template;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 4:12 PM
 */
public class RenderException extends RuntimeException {

    private final String template;
    private final String message;
    private final int line;

    public RenderException(String message, String template, int line) {
        super();
        this.message = message;
        this.line = line;
        this.template = template;
    }

    public RenderException(RenderException cause, String message, String template, int line) {
        super(cause);
        this.message = message;
        this.line = line;
        this.template = template;
    }

    @Override
    public String getMessage() {
        if (template != null) {
            return "RenderException '" + message + "' in "+template+":"+line;
        } else {
            return "RenderException '" + message + "' in line: "+line;
        }
    }

}
