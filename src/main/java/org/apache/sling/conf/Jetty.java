/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.conf;

import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Generated Java based configuration.
 */
@Configuration
public class Jetty {

    @Value("${activemq.conf}")
    private String activemqConf;
    @Value("${activemq.home}")
    private String activemqHome;

    @Bean("securityLoginService")
    public HashLoginService securityLoginService() {
        HashLoginService bean = new HashLoginService();
        bean.setName("ActiveMQRealm");
        bean.setConfig((activemqConf + "/jetty-realm.properties"));
        return bean;
    }

    @Bean("adminSecurityConstraintMapping")
    public ConstraintMapping adminSecurityConstraintMapping(
            @Qualifier("adminSecurityConstraint")
                    Constraint adminSecurityConstraint) {
        ConstraintMapping bean = new ConstraintMapping();
        bean.setConstraint(adminSecurityConstraint);
        bean.setPathSpec("*.action");
        return bean;
    }

    @Bean("rewrite")
    public RewriteHandler rewrite() {
        RewriteHandler bean = new RewriteHandler();
        HashSet<Rule> set0 = new HashSet<>();
        RedirectRegexRule bean1 = new RedirectRegexRule();
        bean1.setRegex("/api/jolokia(.*)");
        bean1.setReplacement("/hawtio/jolokia$1");
        set0.add(bean1);
        bean.setRules(set0.toArray(new Rule[0]));
        return bean;
    }

    @Bean("securityConstraintMapping")
    public ConstraintMapping securityConstraintMapping(
            @Qualifier("securityConstraint")
                    Constraint securityConstraint) {
        ConstraintMapping bean = new ConstraintMapping();
        bean.setConstraint(securityConstraint);
        bean.setPathSpec("/,*.jsp");
        return bean;
    }

    @Bean("contexts")
    public ContextHandlerCollection contexts() {
        return new ContextHandlerCollection();
    }

    @Bean("adminSecurityConstraint")
    public Constraint adminSecurityConstraint() {
        Constraint bean = new Constraint();
        bean.setName("BASIC");
        bean.setRoles(new String[]{"admin"});
        bean.setAuthenticate(true);
        return bean;
    }

    @Bean("securityConstraint")
    public Constraint securityConstraint() {
        Constraint bean = new Constraint();
        bean.setName("BASIC");
        bean.setRoles(new String[]{"user", "admin"});
        bean.setAuthenticate(true);
        return bean;
    }

    @Bean("securityHandler")
    public ConstraintSecurityHandler securityHandler(
            @Qualifier("securityLoginService")
                    HashLoginService securityLoginService,
            @Qualifier("adminSecurityConstraintMapping")
                    ConstraintMapping adminSecurityConstraintMapping,
            @Qualifier("securityConstraintMapping")
                    ConstraintMapping securityConstraintMapping) {
        ConstraintSecurityHandler bean = new ConstraintSecurityHandler();
        bean.setLoginService(securityLoginService);
        bean.setAuthenticator(new BasicAuthenticator());
        ArrayList list0 = new ArrayList();
        list0.add(adminSecurityConstraintMapping);
        list0.add(securityConstraintMapping);
        bean.setConstraintMappings(list0);
        HandlerCollection bean1 = new HandlerCollection();
        ArrayList<Handler> list2 = new ArrayList<>();
        WebAppContext bean3 = new WebAppContext();
        bean3.setContextPath("/hawtio");
        bean3.setWar((activemqHome + "/webapps/hawtio"));
        bean3.setLogUrlOnStart(true);
        list2.add(bean3);
        WebAppContext bean4 = new WebAppContext();
        bean4.setContextPath("/admin");
        bean4.setResourceBase((activemqHome + "/webapps/admin"));
        bean4.setLogUrlOnStart(true);
        list2.add(bean4);
        WebAppContext bean5 = new WebAppContext();
        bean5.setContextPath("/fileserver");
        bean5.setResourceBase((activemqHome + "/webapps/fileserver"));
        bean5.setLogUrlOnStart(true);
        bean5.setParentLoaderPriority(true);
        list2.add(bean5);
        WebAppContext bean6 = new WebAppContext();
        bean6.setContextPath("/api");
        bean6.setResourceBase((activemqHome + "/webapps/api"));
        bean6.setLogUrlOnStart(true);
        list2.add(bean6);
        ResourceHandler bean7 = new ResourceHandler();
        bean7.setDirectoriesListed(false);
        ArrayList<String> list8 = new ArrayList<>();
        list8.add("index.html");
        bean7.setWelcomeFiles(list8.toArray(new String[0]));
        bean7.setResourceBase((activemqHome + "/webapps/"));
        list2.add(bean7);
        DefaultHandler bean9 = new DefaultHandler();
        bean9.setServeIcon(false);
        list2.add(bean9);
        bean1.setHandlers(list2.toArray(new Handler[0]));
        bean.setHandler(bean1);
        return bean;
    }

    @Bean(name = "Server", initMethod = "start", destroyMethod = "stop")
    public Server Server(
            @Qualifier("rewrite")
                    RewriteHandler rewrite,
            @Qualifier("contexts")
                    ContextHandlerCollection contexts,
            @Qualifier("securityHandler")
                    ConstraintSecurityHandler securityHandler) {
        Server bean = new Server();
        ArrayList<Connector> list0 = new ArrayList<>();
        SelectChannelConnector bean1 = new SelectChannelConnector();
        bean1.setPort(8161);
        list0.add(bean1);
        bean.setConnectors(list0.toArray(new Connector[0]));
        HandlerCollection bean2 = new HandlerCollection();
        ArrayList<Handler> list3 = new ArrayList<>();
        list3.add(rewrite);
        list3.add(contexts);
        list3.add(securityHandler);
        bean2.setHandlers(list3.toArray(new Handler[0]));
        bean.setHandler(bean2);
        return bean;
    }

}
