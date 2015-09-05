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
package org.thymeleaf.engine;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.ICDATASection;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IComment;
import org.thymeleaf.model.IDocType;
import org.thymeleaf.model.IElementAttributes;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IMarkup;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IProcessingInstruction;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.model.IXMLDeclaration;
import org.thymeleaf.processor.element.IElementProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.Validate;


/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
public class ImmutableMarkup implements IMarkup {

    private final Markup markup;


    // Package-protected constructor, because we don't want anyone creating these objects from outside the engine.
    // Specifically, they will only be created from the TemplateManager.
    // If a processor (be it standard or custom-made) wants to create a piece of markup, that should be a Markup
    // object, not this.
    ImmutableMarkup(final IEngineConfiguration configuration, final TemplateMode templateMode) {
        super();
        Validate.notNull(configuration, "Engine Configuration cannot be null");
        Validate.notNull(templateMode, "Template Mode cannot be null");
        this.markup = new Markup(configuration, templateMode);
    }


    // Only used internally for creating an immutable version of a Markup object
    ImmutableMarkup(final IMarkup markup) {
        super();
        this.markup = markup.cloneMarkup();
    }


    public final IEngineConfiguration getConfiguration() {
        return this.markup.getConfiguration();
    }


    public final TemplateMode getTemplateMode() {
        return this.markup.getTemplateMode();
    }



    public int size() {
        return this.markup.size();
    }


    public ITemplateEvent get(final int pos) {
        return immutableEvent(this.markup.get(pos));
    }




    // We don't want anyone to have direct access to the underlying Markup object from outside the engine.
    // This will effectively turn our ParsedFragmentMarkup into immutable (though not really) and therefore allow us
    // to confidently cache these objects without worrying that anyone can modify them
    final Markup getInternalMarkup() {
        return this.markup;
    }



    public Markup cloneMarkup() {
        return this.markup.cloneMarkup();
    }



    public final String renderMarkup() {
        return this.markup.renderMarkup();
    }




    @Override
    public String toString() {
        return renderMarkup();
    }



    private static ITemplateEvent immutableEvent(final ITemplateEvent event) {

        if (event instanceof IText) {
            return new ImmutableText((IText)event);
        } else if (event instanceof IOpenElementTag) {
            return new ImmutableOpenElementTag((IOpenElementTag)event);
        } else if (event instanceof ICloseElementTag) {
            return new ImmutableCloseElementTag((ICloseElementTag)event);
        } else if (event instanceof IStandaloneElementTag) {
            return new ImmutableStandaloneElementTag((IStandaloneElementTag)event);
        } else if (event instanceof IDocType) {
            return new ImmutableDocType((IDocType)event);
        } else if (event instanceof IComment) {
            return new ImmutableComment((IComment)event);
        } else if (event instanceof ICDATASection) {
            return new ImmutableCDATASection((ICDATASection)event);
        } else if (event instanceof IXMLDeclaration) {
            return new ImmutableXMLDeclaration((IXMLDeclaration)event);
        } else if (event instanceof IProcessingInstruction) {
            return new ImmutableProcessingInstruction((IProcessingInstruction)event);
        } else {
            throw new TemplateProcessingException(
                    "Cannot process as immutable event of type: " + event.getClass().getName());
        }

    }




    private static void immutableException() {
        throw new UnsupportedOperationException(
                "Modifications are not allowed on immutable events. This event object was returned by an immutable " +
                "implementation of the " + IMarkup.class.getName() + " interface, and no modifications are allowed in " +
                "order to keep cache consistency and improve performance. To modify markup events, convert first your " +
                "immutable markup object to a mutable one by means of the " +
                ImmutableMarkup.class.getName() + "#asMutable() method");
    }



    private static abstract class AbstractImmutableTemplateEvent implements ITemplateEvent {

        private final ITemplateEvent wrapped;

        protected AbstractImmutableTemplateEvent(final ITemplateEvent wrapped) {
            super();
            this.wrapped = wrapped;
        }

        public final boolean hasLocation() {
            return this.wrapped.hasLocation();
        }

        public final String getTemplateName() {
            return this.wrapped.getTemplateName();
        }

        public final int getLine() {
            return this.wrapped.getLine();
        }

        public final int getCol() {
            return this.wrapped.getCol();
        }

        public final void write(final Writer writer) throws IOException {
            this.wrapped.write(writer);
        }

    }


    private static final class ImmutableText
            extends AbstractImmutableTemplateEvent implements IText {

        private final IText wrapped;

        private ImmutableText(final IText wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public String getText() {
            return this.wrapped.getText();
        }

        public int length() {
            return this.wrapped.length();
        }

        public char charAt(final int index) {
            return this.wrapped.charAt(index);
        }

        public boolean isWhitespace() {
            return this.wrapped.isWhitespace();
        }

        public CharSequence subSequence(final int start, final int end) {
            return this.wrapped.subSequence(start, end);
        }

        public void setText(final CharSequence text) {
            ImmutableMarkup.immutableException();
        }

        public IText cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableCDATASection
            extends AbstractImmutableTemplateEvent implements ICDATASection {

        private final ICDATASection wrapped;

        private ImmutableCDATASection(final ICDATASection wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public int length() {
            return this.wrapped.length();
        }

        public char charAt(final int index) {
            return this.wrapped.charAt(index);
        }

        public CharSequence subSequence(final int start, final int end) {
            return this.wrapped.subSequence(start, end);
        }

        public String getCDATASection() {
            return this.wrapped.getCDATASection();
        }

        public String getContent() {
            return this.wrapped.getContent();
        }

        public void setContent(final String content) {
            ImmutableMarkup.immutableException();
        }

        public ICDATASection cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableComment
            extends AbstractImmutableTemplateEvent implements IComment {

        private final IComment wrapped;

        private ImmutableComment(final IComment wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public int length() {
            return this.wrapped.length();
        }

        public char charAt(final int index) {
            return this.wrapped.charAt(index);
        }

        public CharSequence subSequence(final int start, final int end) {
            return this.wrapped.subSequence(start, end);
        }

        public String getComment() {
            return this.wrapped.getComment();
        }

        public String getContent() {
            return this.wrapped.getContent();
        }

        public void setContent(final String content) {
            ImmutableMarkup.immutableException();
        }

        public IComment cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableDocType
            extends AbstractImmutableTemplateEvent implements IDocType {

        private final IDocType wrapped;

        private ImmutableDocType(final IDocType wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public String getKeyword() {
            return this.wrapped.getKeyword();
        }

        public String getElementName() {
            return this.wrapped.getElementName();
        }

        public String getType() {
            return this.wrapped.getType();
        }

        public String getPublicId() {
            return this.wrapped.getPublicId();
        }

        public String getSystemId() {
            return this.wrapped.getSystemId();
        }

        public String getInternalSubset() {
            return this.wrapped.getInternalSubset();
        }

        public String getDocType() {
            return this.wrapped.getDocType();
        }

        public void setKeyword(final String keyword) {
            ImmutableMarkup.immutableException();
        }

        public void setElementName(final String elementName) {
            ImmutableMarkup.immutableException();
        }

        public void setToHTML5() {
            ImmutableMarkup.immutableException();
        }

        public void setIDs(final String publicId, final String systemId) {
            ImmutableMarkup.immutableException();
        }

        public void setIDs(final String type, final String publicId, final String systemId) {
            ImmutableMarkup.immutableException();
        }

        public void setInternalSubset(final String internalSubset) {
            ImmutableMarkup.immutableException();
        }

        public IDocType cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableProcessingInstruction
            extends AbstractImmutableTemplateEvent implements IProcessingInstruction {

        private final IProcessingInstruction wrapped;

        private ImmutableProcessingInstruction(final IProcessingInstruction wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public String getTarget() {
            return this.wrapped.getTarget();
        }

        public String getContent() {
            return this.wrapped.getContent();
        }

        public String getProcessingInstruction() {
            return this.wrapped.getProcessingInstruction();
        }

        public void setTarget(final String target) {
            ImmutableMarkup.immutableException();
        }

        public void setContent(final String content) {
            ImmutableMarkup.immutableException();
        }

        public IProcessingInstruction cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableXMLDeclaration
            extends AbstractImmutableTemplateEvent implements IXMLDeclaration {

        private final IXMLDeclaration wrapped;

        private ImmutableXMLDeclaration(final IXMLDeclaration wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public String getKeyword() {
            return this.wrapped.getKeyword();
        }

        public String getVersion() {
            return this.wrapped.getVersion();
        }

        public String getEncoding() {
            return this.wrapped.getEncoding();
        }

        public String getStandalone() {
            return this.wrapped.getStandalone();
        }

        public String getXmlDeclaration() {
            return this.wrapped.getXmlDeclaration();
        }

        public void setVersion(final String version) {
            ImmutableMarkup.immutableException();
        }

        public void setEncoding(final String encoding) {
            ImmutableMarkup.immutableException();
        }

        public void setStandalone(final String standalone) {
            ImmutableMarkup.immutableException();
        }

        public IXMLDeclaration cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }


    private static final class ImmutableElementAttributes implements IElementAttributes {

        private final IElementAttributes wrapped;

        private ImmutableElementAttributes(final IElementAttributes wrapped) {
            super();
            this.wrapped = wrapped;
        }

        public int size() {
            return this.wrapped.size();
        }

        public List<String> getAllCompleteNames() {
            return this.wrapped.getAllCompleteNames();
        }

        public List<AttributeName> getAllAttributeNames() {
            return this.wrapped.getAllAttributeNames();
        }

        public boolean hasAttribute(final String completeName) {
            return this.wrapped.hasAttribute(completeName);
        }

        public boolean hasAttribute(final String prefix, final String name) {
            return this.wrapped.hasAttribute(prefix, name);
        }

        public boolean hasAttribute(final AttributeName attributeName) {
            return this.wrapped.hasAttribute(attributeName);
        }

        public String getValue(final String completeName) {
            return this.wrapped.getValue(completeName);
        }

        public String getValue(final String prefix, final String name) {
            return this.wrapped.getValue(prefix, name);
        }

        public String getValue(final AttributeName attributeName) {
            return this.wrapped.getValue(attributeName);
        }

        public AttributeDefinition getAttributeDefinition(final String completeName) {
            return this.wrapped.getAttributeDefinition(completeName);
        }

        public AttributeDefinition getAttributeDefinition(final String prefix, final String name) {
            return this.wrapped.getAttributeDefinition(prefix, name);
        }

        public AttributeDefinition getAttributeDefinition(final AttributeName attributeName) {
            return this.wrapped.getAttributeDefinition(attributeName);
        }

        public ValueQuotes getValueQuotes(final String completeName) {
            return this.wrapped.getValueQuotes(completeName);
        }

        public ValueQuotes getValueQuotes(final String prefix, final String name) {
            return this.wrapped.getValueQuotes(prefix, name);
        }

        public ValueQuotes getValueQuotes(final AttributeName attributeName) {
            return this.wrapped.getValueQuotes(attributeName);
        }

        public boolean hasLocation(final String completeName) {
            return this.wrapped.hasLocation(completeName);
        }

        public boolean hasLocation(final String prefix, final String name) {
            return this.wrapped.hasAttribute(prefix, name);
        }

        public boolean hasLocation(final AttributeName attributeName) {
            return this.wrapped.hasLocation(attributeName);
        }

        public int getLine(final String completeName) {
            return this.wrapped.getLine(completeName);
        }

        public int getLine(final String prefix, final String name) {
            return this.wrapped.getLine(prefix, name);
        }

        public int getLine(final AttributeName attributeName) {
            return this.wrapped.getLine(attributeName);
        }

        public int getCol(final String completeName) {
            return this.wrapped.getCol(completeName);
        }

        public int getCol(final String prefix, final String name) {
            return this.wrapped.getCol(prefix, name);
        }

        public int getCol(final AttributeName attributeName) {
            return this.wrapped.getCol(attributeName);
        }

        public void clearAll() {
            ImmutableMarkup.immutableException();
        }

        public void setAttribute(final String completeName, final String value) {
            ImmutableMarkup.immutableException();
        }

        public void setAttribute(final String completeName, final String value, final ValueQuotes valueQuotes) {
            ImmutableMarkup.immutableException();
        }

        public void removeAttribute(final String prefix, final String name) {
            ImmutableMarkup.immutableException();
        }

        public void removeAttribute(final String completeName) {
            ImmutableMarkup.immutableException();
        }

        public void removeAttribute(final AttributeName attributeName) {
            ImmutableMarkup.immutableException();
        }

        public void write(final Writer writer) throws IOException {
            ImmutableMarkup.immutableException();
        }

    }



    private static abstract class AbstractImmutableElementTag extends AbstractImmutableTemplateEvent implements IElementTag {

        private final IElementTag wrapped;

        protected AbstractImmutableElementTag(final IElementTag wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public ElementDefinition getElementDefinition() {
            return this.wrapped.getElementDefinition();
        }

        public String getElementName() {
            return this.wrapped.getElementName();
        }

    }



    private static abstract class AbstractImmutableProcessableElementTag
            extends AbstractImmutableElementTag implements IProcessableElementTag {

        private final IProcessableElementTag wrapped;

        protected AbstractImmutableProcessableElementTag(final IProcessableElementTag wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public IElementAttributes getAttributes() {
            return new ImmutableElementAttributes(this.wrapped.getAttributes());
        }

        public void precomputeAssociatedProcessors() {
            ImmutableMarkup.immutableException();
        }

        public boolean hasAssociatedProcessors() {
            return this.wrapped.hasAssociatedProcessors();
        }

        public List<IElementProcessor> getAssociatedProcessorsInOrder() {
            return this.wrapped.getAssociatedProcessorsInOrder();
        }

    }



    private static final class ImmutableStandaloneElementTag
            extends AbstractImmutableProcessableElementTag implements IStandaloneElementTag {

        private final IStandaloneElementTag wrapped;

        private ImmutableStandaloneElementTag(final IStandaloneElementTag wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public boolean isMinimized() {
            return this.wrapped.isMinimized();
        }

        public void setMinimized(final boolean minimized) {
            ImmutableMarkup.immutableException();
        }

        public boolean isSynthetic() {
            return this.wrapped.isSynthetic();
        }

        public IStandaloneElementTag cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }



    private static final class ImmutableOpenElementTag
            extends AbstractImmutableProcessableElementTag implements IOpenElementTag {

        private final IOpenElementTag wrapped;

        private ImmutableOpenElementTag(final IOpenElementTag wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public boolean isSynthetic() {
            return this.wrapped.isSynthetic();
        }

        public IOpenElementTag cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }



    private static final class ImmutableCloseElementTag
            extends AbstractImmutableElementTag implements ICloseElementTag {

        private final ICloseElementTag wrapped;

        private ImmutableCloseElementTag(final ICloseElementTag wrapped) {
            super(wrapped);
            this.wrapped = wrapped;
        }

        public boolean isSynthetic() {
            return this.wrapped.isSynthetic();
        }

        public boolean isUnmatched() {
            return this.wrapped.isUnmatched();
        }

        public ICloseElementTag cloneEvent() {
            ImmutableMarkup.immutableException();
            return null;
        }

    }






}