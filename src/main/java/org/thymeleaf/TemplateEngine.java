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
package org.thymeleaf;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.context.IContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.engine.TemplateManager;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.exceptions.TemplateOutputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.text.ITextRepository;
import org.thymeleaf.text.TextRepositories;
import org.thymeleaf.util.LoggingUtils;
import org.thymeleaf.util.Validate;


/**
 * <p>
 *   Main class for the execution of templates.
 * </p>
 * <p>
 *   This is the only implementation of {@link ITemplateEngine} provided out of the box by Thymeleaf.
 * </p>
 *
 * <h3>Creating an instance of <tt>TemplateEngine</tt></h3>
 * <p>
 *   An instance of this class can be created at any time by calling its constructor:
 * </p>
 * <code>
 *   final TemplateEngine templateEngine = new TemplateEngine();
 * </code>
 * <p>
 *   Creation and configuration of <tt>TemplateEngine</tt> instances is expensive, so it is
 *   recommended to create only one instance of this class (or at least one instance per
 *   dialect/configuration) and use it to process multiple templates.
 * </p>
 * 
 * <h3>Configuring the <tt>TemplateEngine</tt></h3>
 * <p>
 *   Once created, an instance of <tt>TemplateEngine</tt> has to be typically configured a
 *   mechanism for <em>resolving templates</em> (i.e. obtaining and reading them):
 * </p>
 * <ul>
 *   <li>One or more <b>Template Resolvers</b> (implementations of {@link ITemplateResolver}), in
 *       charge of reading or obtaining the templates so that the engine is able to process them. If
 *       only one template resolver is set (the most common case), the {@link #setTemplateResolver(ITemplateResolver)}
 *       method can be used for this. If more resolvers are to be set, both the
 *       {@link #setTemplateResolvers(Set)} and {@link #addTemplateResolver(ITemplateResolver)} methods
 *       can be used.</li>
 *   <li>If no <em>Template Resolvers</em> are configured, {@link TemplateEngine} instances will use
 *       a {@link StringTemplateResolver} instance which will consider templates being specified
 *       for processing as the template contents (instead of just their names). This configuration
 *       will be overridden when {@link #setTemplateResolver(ITemplateResolver)}, {@link #setTemplateResolvers(Set)} or
 *       {@link #addTemplateResolver(ITemplateResolver)} are called for the first time.</li>
 * </ul>
 * <p>
 *   Templates will be processed according to a set of configured <strong>Dialects</strong> (implementations
 *   of {@link IDialect}), defining the way in which templates will be processed: processors, expression objects, etc.
 *   If no dialect is explicitly set, a unique instance of {@link org.thymeleaf.standard.StandardDialect}
 *   (the <i>Standard Dialect</i>) will be used.
 * </p>
 * <ul>
 *   <li>Dialects define a <i>default prefix</i>, which will be used for them if not otherwise specified.</li>
 *   <li>When setting/adding dialects, a non-default prefix can be specified for each of them.</li>
 *   <li>Several dialects can use the same prefix, effectively acting as an aggregate dialect.</li>
 * </ul>
 * <p>
 *   Those templates that include any <em>externalized messages</em> (also <em>internationalized messages</em>,
 *   i18n) will need the {@link TemplateEngine} to be configured one or more <b>Message Resolvers</b> (implementations
 *   of {@link IMessageResolver}). If no message resolver is explicitly set, {@link TemplateEngine} will default
 *   to a single instance of {@link StandardMessageResolver}).
 * </p>
 * <p>
 *   If only one message resolver is set, the {@link #setMessageResolver(IMessageResolver)} method
 *   can be used for this. If more resolvers are to be set, both the
 *   {@link #setMessageResolvers(Set)} and {@link #addMessageResolver(IMessageResolver)} methods
 *   can be used.
 * </p>
 * <p>
 *  Besides these, a <b>Cache Manager</b> (implementation of {@link ICacheManager}) can also be configured. The
 *  Cache Manager is in charge of providing the cache objects (instances of {@link org.thymeleaf.cache.ICache}) to
 *  be used for caching (at least) parsed templates and parsed expressions. By default, a {@link StandardCacheManager}
 *  instance is used. If a null cache manager is specified by calling {@link #setCacheManager(ICacheManager)}, no
 *  caches will be used.
 * </p>
 * 
 * <h3>Template Execution</h3>
 * <h4>1. Creating a context</h4>
 * <p>
 *   All template executions require a <i>context</i>. A context is an object that
 *   implements the {@link IContext} interface, and that contains at least the following
 *   data:
 * </p>
 * <ul>
 *   <li>The <i>locale</i> to be used for message externalization (internationalization).</li>
 *   <li>The <i>context variables</i>. A map of variables that will be available for
 *       use from expressions in the executed template.</li>  
 * </ul>
 * <p>
 *   Two {@link IContext} implementations are provided out-of-the-box:
 * </p>
 * <ul>
 *   <li>{@link org.thymeleaf.context.Context}, a standard implementation containing only
 *       the required data.</li>
 *   <li>{@link org.thymeleaf.context.WebContext}, a web-specific implementation 
 *       extending the {@link org.thymeleaf.context.IWebContext} subinterface, offering
 *       access to request, session and servletcontext (application) attributes in special
 *       expression objects. Using an implementation of
 *       {@link org.thymeleaf.context.IWebContext} is required when using Thymeleaf for 
 *       generating HTML interfaces in web applications based on the Servlet API.</li>
 * </ul>
 * <p>
 *   Creating a {@link org.thymeleaf.context.Context} instance is very simple:
 * </p>
 * <code>
 *   final IContext ctx = new Context();<br>
 *   ctx.setVariable("allItems", items);
 * </code>
 * <p>
 *   If you want, you can also specify the locale to be used for processing the template:
 * </p>
 * <code>
 *   final IContext ctx = new Context(new Locale("gl","ES"));<br>
 *   ctx.setVariable("allItems", items);
 * </code>
 * <p>
 *   A {@link org.thymeleaf.context.WebContext} would also need 
 *   {@link javax.servlet.http.HttpServletRequest}, {@link javax.servlet.http.HttpServletResponse} and
 *   {@link javax.servlet.ServletContext} objects as constructor arguments: 
 * </p>
 * <code>
 *   final IContext ctx = new WebContext(request, response, servletContext);<br>
 *   ctx.setVariable("allItems", items);
 * </code>
 * <p>
 *   See the documentation for these specific implementations for more details.
 * </p>
 * 
 * <h4>2. Template Processing</h4>
 * <p>
 *   In order to execute templates, the different <tt>process(...)</tt> methods should
 *   be used. Those are mostly divided into two blocks: those that return the template processing
 *   result as a <tt>String</tt>, and those that receive a {@link Writer} as an argument
 *   and use it for writing the result instead.
 * </p>
 * <p>
 *   Without a writer, the processing result will be returned as a String:
 * </p>
 * <code>
 *   final String result = templateEngine.process("mytemplate", ctx);
 * </code>
 * <p>
 *   By specifying a writer, we can avoid the creation of a String containing the
 *   whole processing result by writing this result into the output stream as soon 
 *   as it is produced from the processed template. This is especially useful (and highly
 *   recommended) in web scenarios:
 * </p>
 * <code>
 *   templateEngine.process("mytemplate", ctx, httpServletResponse.getWriter());
 * </code>
 * <p>
 *   The <tt>"mytemplate"</tt> String argument is the <i>template name</i>, and it
 *   will relate to the physical/logical location of the template itself in a way
 *   configured at the template resolver/s. 
 * </p>
 * <hr>
 * <p>
 *   Note a class with this name existed since 1.0, but it was completely reimplemented
 *   in Thymeleaf 3.0
 * </p>
 *
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 3.0.0
 *
 */
public class TemplateEngine implements ITemplateEngine {

    /**
     * <p>
     *   Name of the <tt>TIMER</tt> logger. This logger will output the time required
     *   for executing each template processing operation.
     * </p>
     * <p>
     *   The value of this constant is <tt>org.thymeleaf.TemplateEngine.TIMER</tt>. This
     *   allows you to set a specific configuration and/or appenders for timing info at your logging
     *   system configuration.
     * </p>
     */
    public static final String TIMER_LOGGER_NAME = TemplateEngine.class.getName() + ".TIMER";

    private static final Logger logger = LoggerFactory.getLogger(TemplateEngine.class);
    private static final Logger timerLogger = LoggerFactory.getLogger(TIMER_LOGGER_NAME);

    private static final int NANOS_IN_SECOND = 1000000;

    private volatile boolean initialized = false;

    private final Set<DialectConfiguration> dialectConfigurations = new LinkedHashSet<DialectConfiguration>(3);
    private final Set<ITemplateResolver> templateResolvers = new LinkedHashSet<ITemplateResolver>(3);
    private final Set<IMessageResolver> messageResolvers = new LinkedHashSet<IMessageResolver>(3);
    private ICacheManager cacheManager = null;

    // TODO Make this configurable!
    private final ITextRepository textRepository = TextRepositories.createLimitedSizeCacheRepository();


    private IEngineConfiguration configuration = null;



    


    /**
     * <p>
     *   Constructor for <tt>TemplateEngine</tt> objects.
     * </p>
     * <p>
     *   This is the only way to create a <tt>TemplateEngine</tt> instance (which
     *   should be configured after creation).
     * </p>
     */
    public TemplateEngine() {
        super();
        setCacheManager(new StandardCacheManager());
        setMessageResolver(new StandardMessageResolver());
        setDialect(new StandardDialect());
        setTemplateResolver(new StringTemplateResolver());
    }



    private void checkNotInitialized() {
        if (this.initialized) {
            throw new IllegalStateException(
                    "Template engine has already been initialized (probably because it has already been executed or " +
                    "a fully-built Configuration object has been requested from it. At this state, no modifications on " +
                    "its configuration are allowed.");
        }
    }









    /**
     * <p>
     *   Internal method that initializes the Template Engine instance. This method
     *   is called before the first execution of {@link #process(String, IContext)}
     *   in order to create all the structures required for a quick execution of
     *   templates.
     * </p>
     * <p>
     *   THIS METHOD IS INTERNAL AND SHOULD <b>NEVER</b> BE CALLED DIRECTLY.
     * </p>
     * <p>
     *   If a subclass of <tt>TemplateEngine</tt> needs additional steps for
     *   initialization, the {@link #initializeSpecific()} method should
     *   be overridden.
     * </p>
     */
    private void initialize() {

        if (!this.initialized) {

            synchronized (this) {

                if (!this.initialized) {

                    logger.debug("[THYMELEAF] INITIALIZING TEMPLATE ENGINE");

                    // We need at least one template resolver at this point - we set the StringTemplateResolver
                    // as default, but someone might have overridden it, so we need to check just in case
                    if (this.templateResolvers == null || this.templateResolvers.isEmpty()) {
                        throw new ConfigurationException(
                                "No Template Resolvers have been configured for this TemplateEngine instance. " +
                                "At least one Template Resolver is required.");
                    }

                    this.configuration =
                            new EngineConfiguration(this.templateResolvers, this.messageResolvers, this.dialectConfigurations, this.cacheManager, this.textRepository);
                    ((EngineConfiguration)this.configuration).initialize();

                    initializeSpecific();

                    this.initialized = true;

                    // Log configuration details
                    ConfigurationPrinterHelper.printConfiguration(this.configuration);

                    logger.debug("[THYMELEAF] TEMPLATE ENGINE INITIALIZED");

                }

            }

        }

    }



    /**
     * <p>
     *   This method performs additional initializations required for a
     *   <tt>TemplateEngine</tt>. It is called by {@link #initialize()}.
     * </p>
     * <p>
     *   The implementation of this method does nothing, and it is designed
     *   for being overridden by subclasses of <tt>TemplateEngine</tt>.
     * </p>
     */
    protected void initializeSpecific() {
        // Nothing to be executed here. Meant for extension
    }



    /**
     * <p>
     *   Checks whether the <tt>TemplateEngine</tt> has already been initialized
     *   or not. A <tt>TemplateEngine</tt> is initialized when the {@link #initialize()}
     *   method is called the first time a template is processed.
     * </p>
     * <p>
     *   Normally, there is no good reason why users would need to call this method.
     * </p>
     *
     * @return <tt>true</tt> if the template engine has already been initialized,
     *         <tt>false</tt> if not.
     */
    public final boolean isInitialized() {
        return this.initialized;
    }




    public IEngineConfiguration getConfiguration() {
        if (!this.initialized) {
            initialize();
        }
        return this.configuration;
    }

    
    /**
     * <p>
     *   Returns the configured dialects, referenced by their prefixes.
     * </p>
     * 
     * @return the {@link IDialect} instances currently configured.
     * @since 3.0.0
     */
    public final Map<String,Set<IDialect>> getDialectsByPrefix() {
        final Set<DialectConfiguration> dialectConfs;
        if (this.initialized) {
            dialectConfs = this.configuration.getDialectConfigurations();
        } else {
            dialectConfs = this.dialectConfigurations;
        }
        final Map<String,Set<IDialect>> dialectsByPrefix = new LinkedHashMap<String, Set<IDialect>>(3);
        for (final DialectConfiguration dialectConfiguration : dialectConfs) {
            final String prefix = dialectConfiguration.getPrefix();
            Set<IDialect> dialectsForPrefix = dialectsByPrefix.get(prefix);
            if (dialectsForPrefix == null) {
                dialectsForPrefix = new LinkedHashSet<IDialect>(2);
                dialectsByPrefix.put(prefix, dialectsForPrefix);
            }
            dialectsForPrefix.add(dialectConfiguration.getDialect());
        }
        return Collections.unmodifiableMap(dialectsByPrefix);
    }
    
    
    /**
     * <p>
     *   Returns the configured dialects.
     * </p>
     * 
     * @return the {@link IDialect} instances currently configured.
     */
    public final Set<IDialect> getDialects() {
        if (this.initialized) {
            return this.configuration.getDialects();
        }
        final Set<IDialect> dialects = new LinkedHashSet<IDialect>(this.dialectConfigurations.size());
        for (final DialectConfiguration dialectConfiguration : this.dialectConfigurations) {
            dialects.add(dialectConfiguration.getDialect());
        }
        return Collections.unmodifiableSet(dialects);
    }

    /**
     * <p>
     *   Sets a new unique dialect for this template engine.
     * </p>
     * <p>
     *   This operation is equivalent to removing all the currently configured dialects and then
     *   adding this one.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param dialect the new unique {@link IDialect} to be used.
     */
    public void setDialect(final IDialect dialect) {
        Validate.notNull(dialect, "Dialect cannot be null");
        checkNotInitialized();
        this.dialectConfigurations.clear();
        this.dialectConfigurations.add(new DialectConfiguration(dialect));
    }

    /**
     * <p>
     *   Adds a new dialect for this template engine, using the specified prefix.
     * </p>
     * <p>
     *   This dialect will be added to the set of currently configured ones.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param prefix the prefix that will be used for this dialect
     * @param dialect the new {@link IDialect} to be added to the existing ones.
     */
    public void addDialect(final String prefix, final IDialect dialect) {
        Validate.notNull(dialect, "Dialect cannot be null");
        checkNotInitialized();
        this.dialectConfigurations.add(new DialectConfiguration(prefix, dialect));
    }

    /**
     * <p>
     *   Adds a new dialect for this template engine, using the dialect's specified
     *   default dialect.
     * </p>
     * <p>
     *   This dialect will be added to the set of currently configured ones.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param dialect the new {@link IDialect} to be added to the existing ones.
     */
    public void addDialect(final IDialect dialect) {
        Validate.notNull(dialect, "Dialect cannot be null");
        checkNotInitialized();
        this.dialectConfigurations.add(new DialectConfiguration(dialect));
    }

    /**
     * <p>
     *   Sets a new set of dialects for this template engine, referenced
     *   by the prefixes they will be using.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param dialects the new map of {@link IDialect} objects to be used, referenced
     *        by their prefixes.
     */
    public void setDialectsByPrefix(final Map<String,IDialect> dialects) {
        Validate.notNull(dialects, "Dialect map cannot be null");
        checkNotInitialized();
        this.dialectConfigurations.clear();
        for (final Map.Entry<String,IDialect> dialectEntry : dialects.entrySet()) {
            addDialect(dialectEntry.getKey(), dialectEntry.getValue());
        }
    }

    /**
     * <p>
     *   Sets a new set of dialects for this template engine, all of them using
     *   their default prefixes.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param dialects the new set of {@link IDialect} objects to be used.
     */
    public void setDialects(final Set<IDialect> dialects) {
        Validate.notNull(dialects, "Dialect set cannot be null");
        checkNotInitialized();
        this.dialectConfigurations.clear();
        for (final IDialect dialect : dialects)  {
            addDialect(dialect);
        }
    }

    
    /**
     * <p>
     *   Sets an additional set of dialects for this template engine, all of them using
     *   their default prefixes.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param additionalDialects the new set of {@link IDialect} objects to be used.
     * 
     * @since 2.0.9
     * 
     */
    public void setAdditionalDialects(final Set<IDialect> additionalDialects) {
        Validate.notNull(additionalDialects, "Dialect set cannot be null");
        checkNotInitialized();
        for (final IDialect dialect : additionalDialects)  {
            addDialect(dialect);
        }
    }

    
    /**
     * <p>
     *   Removes all the currently configured dialects.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     */
    public void clearDialects() {
        checkNotInitialized();
        this.dialectConfigurations.clear();
    }

    

    /**
     * <p>
     *   Returns the Set of template resolvers currently configured.
     * </p>
     * 
     * @return the template resolvers.
     */
    public final Set<ITemplateResolver> getTemplateResolvers() {
        if (this.initialized) {
            return this.configuration.getTemplateResolvers();
        }
        return Collections.unmodifiableSet(this.templateResolvers);
    }

    /**
     * <p>
     *   Sets the entire set of template resolvers.
     * </p>
     * 
     * @param templateResolvers the new template resolvers.
     */
    public void setTemplateResolvers(final Set<ITemplateResolver> templateResolvers) {
        Validate.notNull(templateResolvers, "Template Resolver set cannot be null");
        checkNotInitialized();
        this.templateResolvers.clear();
        for (final ITemplateResolver templateResolver : templateResolvers)  {
            addTemplateResolver(templateResolver);
        }
    }

    /**
     * <p>
     *   Adds a new template resolver to the current set.
     * </p>
     * 
     * @param templateResolver the new template resolver.
     */
    public void addTemplateResolver(final ITemplateResolver templateResolver) {
        Validate.notNull(templateResolver, "Template Resolver cannot be null");
        checkNotInitialized();
        this.templateResolvers.add(templateResolver);
    }

    /**
     * <p>
     *   Sets a single template resolver for this template engine.
     * </p>
     * <p>
     *   Calling this method is equivalent to calling {@link #setTemplateResolvers(Set)}
     *   passing a Set with only one template resolver.
     * </p>
     * 
     * @param templateResolver the template resolver to be set.
     */
    public void setTemplateResolver(final ITemplateResolver templateResolver) {
        Validate.notNull(templateResolver, "Template Resolver cannot be null");
        checkNotInitialized();
        this.templateResolvers.clear();
        this.templateResolvers.add(templateResolver);
    }

    
    /**
     * <p>
     *   Returns the cache manager in effect. This manager is in charge of providing
     *   the various caches needed by the system during its process.
     * </p>
     * <p>
     *   By default, an instance of {@link org.thymeleaf.cache.StandardCacheManager}
     *   is set.
     * </p>
     * 
     * @return the cache manager
     */
    public ICacheManager getCacheManager() {
        if (this.initialized) {
            return this.configuration.getCacheManager();
        }
        return this.cacheManager;
    }
    
    /**
     * <p>
     *   Sets the Cache Manager to be used. If set to null, no caches will be used 
     *   throughout the engine.
     * </p>
     * <p>
     *   By default, an instance of {@link org.thymeleaf.cache.StandardCacheManager}
     *   is set.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param cacheManager the cache manager to be set.
     * 
     */
    public void setCacheManager(final ICacheManager cacheManager) {
        // Can be set to null (= no caches at all)
        checkNotInitialized();
        this.cacheManager = cacheManager;
    }

    
    /**
     * <p>
     *   Returns the set of Message Resolvers configured for this Template Engine.
     * </p>
     * 
     * @return the set of message resolvers.
     */
    public final Set<IMessageResolver> getMessageResolvers() {
        if (this.initialized) {
            return this.configuration.getMessageResolvers();
        }
        return Collections.unmodifiableSet(this.messageResolvers);
    }

    /**
     * <p>
     *   Sets the message resolvers to be used by this template engine.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param messageResolvers the Set of template resolvers.
     */
    public void setMessageResolvers(final Set<IMessageResolver> messageResolvers) {
        Validate.notNull(messageResolvers, "Message Resolver set cannot be null");
        checkNotInitialized();
        this.messageResolvers.clear();
        for (final IMessageResolver messageResolver : messageResolvers)  {
            addMessageResolver(messageResolver);
        }
    }
    
    /**
     * <p>
     *   Adds a message resolver to the set of message resolvers to be used
     *   by the template engine.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param messageResolver the new message resolver to be added.
     */
    public void addMessageResolver(final IMessageResolver messageResolver) {
        Validate.notNull(messageResolver, "Message Resolver cannot be null");
        checkNotInitialized();
        this.messageResolvers.add(messageResolver);
    }

    /**
     * <p>
     *   Sets a single message resolver for this template engine.
     * </p>
     * <p>
     *   Calling this method is equivalent to calling {@link #setMessageResolvers(Set)}
     *   passing a Set with only one message resolver.
     * </p>
     * <p>
     *   This operation can only be executed before processing templates for the first
     *   time. Once a template is processed, the template engine is considered to be
     *   <i>initialized</i>, and from then on any attempt to change its configuration
     *   will result in an exception.
     * </p>
     * 
     * @param messageResolver the message resolver to be set.
     */
    public void setMessageResolver(final IMessageResolver messageResolver) {
        Validate.notNull(messageResolver, "Message Resolver cannot be null");
        checkNotInitialized();
        this.messageResolvers.clear();
        this.messageResolvers.add(messageResolver);
    }

    
    
    

    
    
    /**
     * <p>
     *   Completely clears the Template Cache.
     * </p>
     * <p>
     *   If this method is called before the TemplateEngine has been initialized,
     *   it causes its initialization.
     * </p>
     */
    public void clearTemplateCache() {
        if (!this.initialized) {
            initialize();
        }
        this.configuration.getTemplateManager().clearCaches();
    }


    /**
     * <p>
     *   Clears the entry in the Template Cache for the specified
     *   template, if it is currently cached.
     * </p>
     * <p>
     *   If this method is called before the TemplateEngine has been initialized,
     *   it causes its initialization.
     * </p>
     * 
     * @param templateName the name of the template to be cleared from cache.
     */
    public void clearTemplateCacheFor(final String templateName) {
        Validate.notNull(templateName, "Template name cannot be null");
        if (!this.initialized) {
            initialize();
        }
        this.configuration.getTemplateManager().clearCachesFor(templateName);
    }
    
    
    

    
    
    /**
     * <p>
     *   Internal method that retrieves the thread name/index for the
     *   current template execution. 
     * </p>
     * <p>
     *   THIS METHOD IS INTERNAL AND SHOULD <b>NEVER</b> BE CALLED DIRECTLY.
     * </p>
     * 
     * @return the index of the current execution.
     */
    public static String threadIndex() {
        return Thread.currentThread().getName();
    }




    public final String process(final String template, final IContext context) {
        return process(new TemplateSpec(template, null, null, null), context);
    }


    public final String process(final String template, final Set<String> templateSelectors, final IContext context) {
        return process(new TemplateSpec(template, templateSelectors, null, null), context);
    }


    public final String process(final TemplateSpec templateSpec, final IContext context) {
        final StringWriter stringWriter = new StringWriter();
        process(templateSpec, context, stringWriter);
        return stringWriter.toString();
    }


    public final void process(final String template, final IContext context, final Writer writer) {
        process(new TemplateSpec(template, null, null, null), context, writer);
    }


    public final void process(final String template, final Set<String> templateSelectors, final IContext context, final Writer writer) {
        process(new TemplateSpec(template, templateSelectors, null, null), context, writer);
    }


    public final void process(final TemplateSpec templateSpec, final IContext context, final Writer writer) {

        if (!this.initialized) {
            initialize();
        }
        
        try {
            
            Validate.notNull(templateSpec, "Template Specification cannot be null");
            Validate.notNull(context, "Context cannot be null");
            Validate.notNull(writer, "Writer cannot be null");
            // selectors CAN actually be null if we are going to render the entire template
            // templateMode CAN also be null if we are going to use the mode specified by the template resolver

            if (logger.isTraceEnabled()) {
                logger.trace("[THYMELEAF][{}] STARTING PROCESS OF TEMPLATE \"{}\" WITH LOCALE {}",
                        new Object[]{TemplateEngine.threadIndex(), templateSpec, context.getLocale()});
            }

            final long startNanos = System.nanoTime();

            final TemplateManager templateManager = this.configuration.getTemplateManager();
            templateManager.parseAndProcessStandalone(template, templateSelectors, templateMode, context, writer, true);

            final long endNanos = System.nanoTime();
            
            if (logger.isTraceEnabled()) {
                logger.trace("[THYMELEAF][{}] FINISHED PROCESS AND OUTPUT OF TEMPLATE \"{}\" WITH LOCALE {}",
                        new Object[]{TemplateEngine.threadIndex(), templateSpec, context.getLocale()});
            }

            if (timerLogger.isTraceEnabled()) {
                final BigDecimal elapsed = BigDecimal.valueOf(endNanos - startNanos);
                final BigDecimal elapsedMs = elapsed.divide(BigDecimal.valueOf(NANOS_IN_SECOND), RoundingMode.HALF_UP);
                timerLogger.trace(
                        "[THYMELEAF][{}][{}][{}][{}][{}] TEMPLATE \"{}\" WITH LOCALE {} PROCESSED IN {} nanoseconds (approx. {}ms)",
                        new Object[]{
                                TemplateEngine.threadIndex(),
                                LoggingUtils.loggifyTemplateName(templateSpec.getTemplate()), context.getLocale(), elapsed, elapsedMs,
                                templateSpec, context.getLocale(), elapsed, elapsedMs});
            }
            
        } catch (final TemplateOutputException e) {

            // We log the exception just in case higher levels do not end up logging it (e.g. they could simply display traces in the browser
            logger.error(String.format("[THYMELEAF][%s] Exception processing template \"%s\": %s", new Object[] {TemplateEngine.threadIndex(), templateSpec, e.getMessage()}), e);
            throw e;
            
        } catch (final TemplateEngineException e) {

            // We log the exception just in case higher levels do not end up logging it (e.g. they could simply display traces in the browser
            logger.error(String.format("[THYMELEAF][%s] Exception processing template \"%s\": %s", new Object[] {TemplateEngine.threadIndex(), templateSpec, e.getMessage()}), e);
            throw e;
            
        } catch (final RuntimeException e) {

            // We log the exception just in case higher levels do not end up logging it (e.g. they could simply display traces in the browser
            logger.error(String.format("[THYMELEAF][%s] Exception processing template \"%s\": %s", new Object[] {TemplateEngine.threadIndex(), templateSpec, e.getMessage()}), e);
            throw new TemplateProcessingException("Exception processing template", templateSpec.toString(), e);
            
        }
        
    }





}
