/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
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
package org.thymeleaf.templateengine.processors.dialects.replacewithnonprocessable;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

public class ReplaceWithNonProcessableDialect extends AbstractProcessorDialect {

    public static final String PREFIX = "precedence";

    public ReplaceWithNonProcessableDialect() {
        super(ReplaceWithNonProcessableDialect.class.getSimpleName(), PREFIX, 1000);
    }


    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new ReplaceWithNonProcessableCDATASectionProcessor());
        processors.add(new ReplaceWithNonProcessableCommentProcessor());
        processors.add(new ReplaceWithNonProcessableDocTypeProcessor());
        processors.add(new ReplaceWithNonProcessableProcessingInstructionProcessor());
        processors.add(new ReplaceWithNonProcessableTextProcessor());
        processors.add(new ReplaceWithNonProcessableXMLDeclarationProcessor());
        return processors;
    }

}
