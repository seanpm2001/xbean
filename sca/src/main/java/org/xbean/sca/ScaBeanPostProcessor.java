/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.xbean.sca;

import org.osoa.sca.ModuleContext;
import org.osoa.sca.annotations.ComponentMetaData;
import org.osoa.sca.annotations.ComponentName;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.SessionID;
import org.osoa.sca.model.Component;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Performs SCA based dependency injection rules.
 * 
 * @version $Revision$
 */
public class ScaBeanPostProcessor extends IntrospectionSupport implements DestructionAwareBeanPostProcessor, BeanFactoryPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        List methods = findMethodsWithAnnotation(bean, Init.class);
        invokeVoidMethods(bean, methods);
        processFields(bean, beanName);
        processProperties(bean, beanName);
        return bean;
    }

    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        List methods = findMethodsWithAnnotation(bean, Destroy.class);
        invokeVoidMethods(bean, methods);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            String beanName = beanNames[i];
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            postProcessBeanDefinition(beanFactory, beanName, beanDefinition);
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void postProcessBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName, BeanDefinition definition) throws BeansException {
        BeanInfo beanInfo = getBeanInfo(beanFactory.getType(beanName));
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            processProperty(beanName, definition, descriptor);
        }
    }

    protected void processProperty(String beanName, BeanDefinition definition, PropertyDescriptor descriptor) throws BeansException {
        Method method = descriptor.getWriteMethod();
        if (method != null) {
            // TODO should we handle the property.name() attribute?
            // maybe add this to XBean code generator...

            Property property = method.getAnnotation(Property.class);
            if (property.required()) {
                // TODO use property.name()?
                String propertyName = descriptor.getName();
                MutablePropertyValues propertyValues = definition.getPropertyValues();
                if (!propertyValues.contains(propertyName)) {
                    throw new BeanInitializationException("Mandatory property: " + propertyName + " not specified");
                }
            }

            Reference reference = method.getAnnotation(Reference.class);
            if (reference.required()) {
                // TODO use reference.name()?
                String propertyName = descriptor.getName();
                MutablePropertyValues propertyValues = definition.getPropertyValues();
                if (!propertyValues.contains(propertyName)) {
                    throw new BeanInitializationException("Mandatory reference: " + propertyName + " not specified");
                }
            }
        }
    }

    protected void processProperties(Object bean, String beanName) throws BeansException {
        BeanInfo beanInfo = getBeanInfo(bean);
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            processProperty(bean, beanName, descriptor);
        }
    }

    protected void processProperty(Object bean, String beanName, PropertyDescriptor descriptor) throws BeansException {
        Method method = descriptor.getWriteMethod();
        if (method != null) {
            if (hasAnnotation(method, ComponentName.class)) {
                Object[] arguments = new Object[] { beanName };
                invokeMethod(bean, method, arguments);
            }
            if (hasAnnotation(method, ComponentMetaData.class)) {
                Object[] arguments = new Object[] { getComponentMetaData(bean, beanName) };
                invokeMethod(bean, method, arguments);
            }
            if (hasAnnotation(method, Context.class)) {
                Object[] arguments = new Object[] { getComponentContext(bean, beanName) };
                invokeMethod(bean, method, arguments);
            }
            if (hasAnnotation(method, SessionID.class)) {
                Object[] arguments = new Object[] { getBeanSessionID(bean, beanName) };
                invokeMethod(bean, method, arguments);
            }
        }
    }

    protected void processFields(Object bean, String beanName) throws BeansException {
        Class type = bean.getClass();
        while (true) {
            Field[] declaredFields = type.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                processField(bean, beanName, field);
            }
            if (type.equals(Object.class)) {
                break;
            }
            else {
                type = type.getSuperclass();
            }
        }
    }

    protected void processField(Object bean, String beanName, Field field) {
        if (hasAnnotation(field, ComponentName.class)) {
            setField(bean, field, beanName);
        }
        if (hasAnnotation(field, ComponentMetaData.class)) {
            setField(bean, field, getComponentMetaData(bean, beanName));
        }
        if (hasAnnotation(field, Context.class)) {
            setField(bean, field, getComponentContext(bean, beanName));
        }
        if (hasAnnotation(field, SessionID.class)) {
            setField(bean, field, getBeanSessionID(bean, beanName));
        }

    }

    protected Component getComponentMetaData(Object bean, String beanName) {
        throw new RuntimeException("TODO: Not Implemented yet");
    }

    protected ModuleContext getComponentContext(Object bean, String beanName) {
        throw new RuntimeException("TODO: Not Implemented yet");
    }

    protected Object getBeanSessionID(Object bean, String beanName) throws BeansException {
        throw new RuntimeException("TODO: Not Implemented yet");
    }

}
