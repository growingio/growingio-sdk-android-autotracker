package com.growingio.sdk.plugin.autotrack.compile.visitor;

import com.google.common.truth.Truth;
import com.growingio.sdk.plugin.autotrack.ByteCodeClassLoader;
import com.growingio.sdk.plugin.autotrack.ClassUtils;
import com.growingio.sdk.plugin.autotrack.compile.Context;
import com.growingio.sdk.plugin.autotrack.compile.SystemLog;
import com.growingio.sdk.plugin.autotrack.hook.HookClassesConfig;
import com.growingio.sdk.plugin.autotrack.hook.InjectMethod;
import com.growingio.sdk.plugin.autotrack.hook.TargetClass;
import com.growingio.sdk.plugin.autotrack.hook.TargetMethod;
import com.growingio.sdk.plugin.autotrack.tmp.Callback;
import com.growingio.sdk.plugin.autotrack.tmp.SubOverrideExample;
import com.growingio.sdk.plugin.autotrack.tmp.SuperExample;
import com.growingio.sdk.plugin.autotrack.tmp.inject.InjectAgent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HookClassesConfig.class})
public class InjectAroundClassVisitorTest {
    @Before
    public void setUp() {
        PowerMockito.mockStatic(HookClassesConfig.class);
    }

    private void mockAroundHookClasses(boolean isAfter) {
        Map<String, TargetClass> targetClassMap = new HashMap<>();
        String className = ClassUtils.getClassName(SuperExample.class);
        TargetClass targetClass = new TargetClass(className);
        targetClassMap.put(className, targetClass);
        TargetMethod targetMethod = new TargetMethod("originExecuteWithArg", "(Ljava/lang/String;)V");
        targetClass.addTargetMethod(targetMethod);
        targetMethod.addInjectMethod(new InjectMethod(ClassUtils.getClassName(InjectAgent.class), "onExecute", "(L" + className + ";Ljava/lang/String;)V", isAfter));
        PowerMockito.when(HookClassesConfig.getAroundHookClasses()).thenReturn(targetClassMap);
    }

    public SuperExample getSpySubExample(Class<?> clazz) throws IOException, IllegalAccessException, InstantiationException {
        InputStream resourceAsStream = ClassUtils.classToInputStream(clazz);
        ClassReader cr = new ClassReader(resourceAsStream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        Context context = new Context(new SystemLog(), getClass().getClassLoader());
        TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
        cr.accept(new InjectAroundClassVisitor(cv, context), ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES);
        Class<?> aClass = new ByteCodeClassLoader(getClass().getClassLoader()).defineClass(clazz.getName(), cw.toByteArray());
        return (SuperExample) PowerMockito.spy(aClass.newInstance());
    }

    @Test
    public void injectAroundBefore_override() throws IllegalAccessException, IOException, InstantiationException, NoSuchFieldException {
        mockAroundHookClasses(false);

        final SuperExample spySubExample = getSpySubExample(SubOverrideExample.class);
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

        InOrder inOrder = Mockito.inOrder(spySubExample);
        inOrder.verify(spySubExample).onExecute();
        inOrder.verify(spySubExample).preOriginExecute();
        inOrder.verify(spySubExample).originExecuteWithArg(Mockito.eq("arg"));
    }

    @Test
    public void injectAroundAfter_override() throws IllegalAccessException, IOException, InstantiationException, NoSuchFieldException {
        mockAroundHookClasses(true);

        final SuperExample spySubExample = getSpySubExample(SubOverrideExample.class);
        final SuperExample[] callbackResult = new SuperExample[1];
        InjectAgent.setCallback(new Callback() {
            @Override
            public void onCallback(SuperExample example) {
                Truth.assertThat(spySubExample == example).isTrue();
                Truth.assertThat(example.isExecuted()).isFalse();
                callbackResult[0] = example;
                example.postOriginExecute();
            }
        });
        spySubExample.onExecute();
        Field isExecuted = SuperExample.class.getDeclaredField("mIsExecuted");
        isExecuted.setAccessible(true);
        Truth.assertThat(callbackResult[0].isExecuted()).isTrue();

        InOrder inOrder = Mockito.inOrder(spySubExample);
        inOrder.verify(spySubExample).onExecute();
        inOrder.verify(spySubExample).originExecuteWithArg(Mockito.eq("arg"));
        inOrder.verify(spySubExample).postOriginExecute();
    }
}
