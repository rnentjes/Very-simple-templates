package nl.astraeus.template;

import java.util.*;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:56 PM
 */
public class ForEachPart extends TemplatePart {

    private List<TemplatePart> parts;
    private List<TemplatePart> altParts;
    private List<TemplatePart> firstParts;
    private List<TemplatePart> lastParts;

    private static enum CurrentPart {
        MAIN,
        FIRST,
        ALT,
        LAST
    }

    private CurrentPart currentPart;

    private String [] modelParts;
    private String parameterName;

    public ForEachPart(int line, String modelObject, String parameterName) {
        super(line);

        this.modelParts = modelObject.split("\\.");
        this.parameterName = parameterName;

        this.parts = null;
        this.altParts = null;
        this.firstParts = null;
        this.lastParts = null;

        currentPart = CurrentPart.MAIN;
    }

    public void setCurrentParts(List<TemplatePart> parts) {
        switch(currentPart) {
            case MAIN:
                if (this.parts != null) {
                    throw new ParseException("Encountered double main part in foreach", getLine());
                } else {
                    this.parts = parts;
                }
                break;
            case ALT:
                if (this.altParts != null) {
                    throw new ParseException("Encountered double alt part in foreach", getLine());
                } else {
                    this.altParts = parts;
                }
                break;
            case FIRST:
                if (this.firstParts != null) {
                    throw new ParseException("Encountered double first part in foreach", getLine());
                } else {
                    this.firstParts = parts;
                }
                break;
            case LAST:
                if (this.lastParts != null) {
                    throw new ParseException("Encountered double last part in foreach", getLine());
                } else {
                    this.lastParts = parts;
                }
                break;
            default:
                throw new ParseException("Unknown current part in foreach!", getLine());

        }
    }

    public void setIsMainPart() {
        currentPart = CurrentPart.MAIN;
    }

    public void setIsAltPart() {
        currentPart = CurrentPart.ALT;
    }

    public void setIsFirstPart() {
        currentPart = CurrentPart.FIRST;
    }

    public void setIsLastPart() {
        currentPart = CurrentPart.LAST;
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        boolean alt = true;
        boolean first = true;

        Map<String, Object> tmpModel = new HashMap<String, Object>(model);

        tmpModel.remove(modelParts[0]);
        Object value = getValueFromModel(model, modelParts);

        if (value instanceof Iterable) {
            Iterator it = ((Iterable)value).iterator();

            while(it.hasNext()) {
                Object object = it.next();
                tmpModel.put(parameterName, object);

                if (first && firstParts != null) {
                    renderParts(firstParts, tmpModel,result);
                } else if (!it.hasNext() && lastParts != null) {
                    renderParts(lastParts, tmpModel,result);
                } else if (alt && altParts != null) {
                    renderParts(altParts, tmpModel, result);
                } else if (parts != null) {
                    renderParts(parts, tmpModel, result);
                } else {
                    throw new RenderException("Can't find current block to render in foreach", getLine());
                }

                alt = !alt;
                first = false;
            }
        }
    }
}
