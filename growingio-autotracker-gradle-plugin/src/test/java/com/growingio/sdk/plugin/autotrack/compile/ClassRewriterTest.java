package com.growingio.sdk.plugin.autotrack.compile;

import com.google.common.truth.Truth;
import com.growingio.sdk.plugin.autotrack.ByteCodeClassLoader;
import com.growingio.sdk.plugin.autotrack.ClassUtils;
import com.growingio.sdk.plugin.autotrack.compile.ClassRewriter;
import com.growingio.sdk.plugin.autotrack.compile.SystemLog;
import com.growingio.sdk.plugin.autotrack.hook.HookClassesConfig;
import com.growingio.sdk.plugin.autotrack.hook.InjectMethod;
import com.growingio.sdk.plugin.autotrack.hook.TargetClass;
import com.growingio.sdk.plugin.autotrack.hook.TargetMethod;
import com.growingio.sdk.plugin.autotrack.tmp.Callback;
import com.growingio.sdk.plugin.autotrack.tmp.LambdaInterface;
import com.growingio.sdk.plugin.autotrack.tmp.LambdaInterfaceExample;
import com.growingio.sdk.plugin.autotrack.tmp.SubExample;
import com.growingio.sdk.plugin.autotrack.tmp.SuperExample;
import com.growingio.sdk.plugin.autotrack.tmp.inject.InjectAgent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HookClassesConfig.class})
public class ClassRewriterTest {
    @Before
    public void setUp() {
        PowerMockito.mockStatic(HookClassesConfig.class);
    }

    private void mockSuperHookClasses(boolean isAfter) {
        Map<String, TargetClass> targetClassMap = new HashMap<>();
        String className = ClassUtils.getClassName(SuperExample.class);
        TargetClass targetClass = new TargetClass(className);
        targetClassMap.put(className, targetClass);
        TargetMethod targetMethod = new TargetMethod("onExecute", "()V");
        targetClass.addTargetMethod(targetMethod);
        targetMethod.addInjectMethod(new InjectMethod(ClassUtils.getClassName(InjectAgent.class), "onExecute", "(L" + className + ";)V", isAfter));
        PowerMockito.when(HookClassesConfig.getSuperHookClasses()).thenReturn(targetClassMap);
    }

    private void mockAroundHookClasses(boolean isAfter) {
        Map<String, TargetClass> targetClassMap = new HashMap<>();
        String className = ClassUtils.getClassName(LambdaInterface.class);
        TargetClass targetClass = new TargetClass(className);
        targetClassMap.put(className, targetClass);
        TargetMethod targetMethod = new TargetMethod("onExecute", "()V");
        targetClass.addTargetMethod(targetMethod);
        targetMethod.addInjectMethod(new InjectMethod(ClassUtils.getClassName(InjectAgent.class), "onExecute", "(L" + className + ";)V", isAfter));
        PowerMockito.when(HookClassesConfig.getAroundHookClasses()).thenReturn(targetClassMap);
    }

    public SuperExample getSpySubExample(Class<?> clazz) throws IOException, IllegalAccessException, InstantiationException {
        InputStream resourceAsStream = ClassUtils.classToInputStream(clazz);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ClassRewriter(new SystemLog(), getClass().getClassLoader(), null).transformClass(resourceAsStream, outputStream);
        Class<?> aClass = new ByteCodeClassLoader(getClass().getClassLoader()).defineClass(clazz.getName(), outputStream.toByteArray());
        return (SuperExample) PowerMockito.spy(aClass.newInstance());
    }

    @Test
    public void inject_superExample() throws IllegalAccessException, IOException, InstantiationException, NoSuchFieldException {
        mockSuperHookClasses(false);

        final SuperExample spySubExample = getSpySubExample(SubExample.class);
        final SuperExample[] callbackResult = new SuperExample[1];
        InjectAgent.setCallback(new Callback() {
            @Override
            public void onCallback(SuperExample example) {
                Truth.assertThat(spySubExample == example).isTrue();
                Truth.assertThat(example.isExecuted()).isFalse();
                callbackResult[0] = example;
                example.preOriginExecute();
            }
        });
        spySubExample.onExecute();
        Field isExecuted = SuperExample.class.getDeclaredField("mIsExecuted");
        isExecuted.setAccessible(true);
        Truth.assertThat(callbackResult[0].isExecuted()).isTrue();

        Mockito.verify(spySubExample, Mockito.times(1)).preOriginExecute();
    }

    @Test
    public void inject_lambdaInterface() throws IllegalAccessException, IOException, InstantiationException, NoSuchFieldException {
        mockAroundHookClasses(false);

        final SuperExample spySubExample = getSpySubExample(LambdaInterfaceExample.class);

        spySubExample.onExecute();
    }
}
