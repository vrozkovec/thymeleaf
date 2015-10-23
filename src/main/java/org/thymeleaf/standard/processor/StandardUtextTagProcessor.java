/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.standard.processor;

import java.util.Set;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.0.0
 *
 */
public final class StandardUtextTagProcessor extends AbstractAttributeTagProcessor {

    public static final int PRECEDENCE = 1400;
    public static final String ATTR_NAME = "utext";


    public StandardUtextTagProcessor(final IProcessorDialect dialect, final TemplateMode templateMode, final String dialectPrefix) {
        super(dialect, templateMode, dialectPrefix, null, false, ATTR_NAME, true, PRECEDENCE, true);
    }


    @Override
    protected void doProcess(
            final ITemplateContext context,
            final IProcessableElementTag tag,
            final AttributeName attributeName,
            final String attributeValue,
            final String attributeTemplateName, final int attributeLine, final int attributeCol,
            final IElementTagStructureHandler structureHandler) {

        final IEngineConfiguration configuration = context.getConfiguration();

        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);

        final IStandardExpression expression = expressionParser.parseExpression(context, attributeValue);
        final Object expressionResult = expression.execute(context, StandardExpressionExecutionContext.RESTRICTED);

        final String unescapedText = (expressionResult == null ? "" : expressionResult.toString());

        /*
         * We will check if there are configured post processors or not. The reason we do this is because output
         * inserted as a result of a th:utext attribute, even if it might be markup, will never be considered as
         * 'processable', i.e. no other processors/inliner will ever be able to act on it. The main reason for this
         * is to protect against code injection.
         *
         * So the only other agents that would be able to modify these th:utext results are POST-PROCESSORS. And
         * they will indeed need markup to have been parsed in order to separate text from structures, so that's why
         * we check if there actually are any post-processors and, if not (most common case), simply output the
         * expression result as if it were a mere (unescaped) text node.
         */
        final Set<IPostProcessor> postProcessors = configuration.getPostProcessors(getTemplateMode());
        if (postProcessors.isEmpty()) {
            structureHandler.setBody(unescapedText, false);
            return;
        }


        /*
         * We have post-processors, so from here one we will have to decide whether we need to parse the unescaped
         * text or not...
         */

        if (!mightContainStructures(unescapedText)) {
            // If this text contains no markup structures, there would be no need to parse it or treat it as markup!
            structureHandler.setBody(unescapedText, false);
            return;
        }

        final TemplateModel parsedFragment =
                configuration.getTemplateManager().parseString(
                        attributeTemplateName, unescapedText,
                        0, 0, // we won't apply offset here because the inserted text does not really come from the template itself
                        context.getTemplateMode(),
                        false); // useCache == false because we could potentially pollute the cache with too many entries (th:utext is too variable!)

        // Setting 'processable' to false avoiding text inliners processing already generated text,
        // which in turn avoids code injection.
        structureHandler.setBody(parsedFragment, false);


    }


    /*
     * This method will be used for determining if we actually need to apply a parser to the unescaped text that we
     * are going to use a a result of this th:utext execution. If there is no '>' character in it, then it is
     * nothing but a piece of text, and applying the parser would be overkill
     */
    private static boolean mightContainStructures(final String unescapedText) {
        int n = unescapedText.length();
        char c;
        while (n-- != 0) {
            c = unescapedText.charAt(n);
            if (c == '>' || c == ']') {
                // Might be the end of a structure!
                return true;
            }
        }
        return false;
    }


}
