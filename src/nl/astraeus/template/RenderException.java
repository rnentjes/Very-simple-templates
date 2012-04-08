package nl.astraeus.template;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 4:12 PM
 */
public class RenderException extends RuntimeException {

    private String message;
    private int line;

    public RenderException(String message, int line) {
        super();
        this.message = message;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return "RenderException '" + message + "' in line: "+line;
    }

}
