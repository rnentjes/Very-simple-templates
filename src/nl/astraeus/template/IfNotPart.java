package nl.astraeus.template;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:56 PM
 */
public class IfNotPart extends IfPart {

    public IfNotPart(String ifCondition) {
        super(ifCondition);

        this.ifCondition = new BooleanNotCondition(ifCondition);
    }
}
