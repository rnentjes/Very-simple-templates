package nl.astraeus.template;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:56 PM
 */
public class IfNotPart extends IfPart {

    public IfNotPart(int line, String templateName, String ifCondition) {
        super(line, templateName, ifCondition);

        this.ifCondition = new BooleanNotCondition(ifCondition);
    }
}
