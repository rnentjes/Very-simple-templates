package nl.astraeus.template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:56 PM
 */
public class ForEachPart extends TemplatePart {

    private TemplatePart [] parts;
    private TemplatePart [] altParts;
    private TemplatePart [] firstParts;
    private TemplatePart [] lastParts;

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
                    //this.parts.addAll(parts);
                    TemplatePart [] orig = this.parts;
                    this.parts = new TemplatePart[orig.length+parts.size()];
                    int index = 0;
                    for (TemplatePart part : orig) {
                        this.parts[index++] = part;
                    }
                    for (TemplatePart part : parts) {
                        this.parts[index++] = part;
                    }
                } else {
                    this.parts = parts.toArray(new TemplatePart[parts.size()]);
                }
                break;
            case ALT:
                if (this.altParts != null) {
                    throw new ParseException("Encountered double alt part in foreach", getLine());
                } else {
                    this.altParts = parts.toArray(new TemplatePart[parts.size()]);
                }
                break;
            case FIRST:
                if (this.firstParts != null) {
                    throw new ParseException("Encountered double first part in foreach", getLine());
                } else {
                    this.firstParts = parts.toArray(new TemplatePart[parts.size()]);
                }
                break;
            case LAST:
                if (this.lastParts != null) {
                    throw new ParseException("Encountered double last part in foreach", getLine());
                } else {
                    this.lastParts = parts.toArray(new TemplatePart[parts.size()]);
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

        //tmpModel.remove(modelParts[0]);
        Object value = getValueFromModel(model, modelParts);

        if (value instanceof Iterable) {
            Iterator it = ((Iterable)value).iterator();

            while(it.hasNext()) {
                Object object = it.next();
                tmpModel.put(parameterName, object);

                tmpModel.put("eachfirst", first);
                tmpModel.put("eachalt", alt);
                tmpModel.put("eachlast", !it.hasNext());

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
