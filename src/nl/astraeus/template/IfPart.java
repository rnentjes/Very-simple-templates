package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:56 PM
 */
public class IfPart extends TemplatePart {

    protected BooleanCondition ifCondition;
    private TemplatePart [] ifParts;
    private TemplatePart [] elseParts;
    private boolean hasElse;
    private String conditionText;

    public IfPart(int line, String ifCondition) {
        super(line);

        this.conditionText = ifCondition;
        this.ifCondition = new BooleanCondition(ifCondition);
        this.ifParts = new TemplatePart [0];
        this.elseParts = new TemplatePart [0];
    }

    public void setIfParts(List<TemplatePart> ifParts) {
        this.ifParts = ifParts.toArray(new TemplatePart[ifParts.size()]);
    }

    public TemplatePart [] getIfParts() {
        return ifParts;
    }

    public void setElseParts(List<TemplatePart> elseParts) {
        this.elseParts = elseParts.toArray(new TemplatePart[elseParts.size()]);
    }

    public boolean isHasElse() {
        return hasElse;
    }

    public void setHasElse(boolean hasElse) {
        this.hasElse = hasElse;
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        try {
            if (ifCondition.evaluate(model)) {
                renderParts(ifParts, model, result);
            } else {
                renderParts(elseParts, model, result);
            }
        } catch (IllegalArgumentException e) {
            throw new RenderException("Can't evaluate condition ("+conditionText+")", getLine());
        }
    }
}
