package com.growingio.sdk.plugin.autotrack.compile.visitor;

import com.google.common.truth.Truth;
import com.growingio.sdk.plugin.autotrack.ClassUtils;
import com.growingio.sdk.plugin.autotrack.compile.Context;
import com.growingio.sdk.plugin.autotrack.compile.SystemLog;
import com.growingio.sdk.plugin.autotrack.tmp.SubExample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;

@RunWith(PowerMockRunner.class)
public class ContextClassVisitorTest {

    @Test
    public void test() throws IOException {
        InputStream resourceAsStream = ClassUtils.classToInputStream(SubExample.class);
        ClassReader cr = new ClassReader(resourceAsStream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        Context context = new Context(new SystemLog(), getClass().getClassLoader());
        cr.accept(new ContextClassVisitor(context), ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES);
        String className = context.getClassName();
        System.out.println(className);
        Truth.assertThat("com/growingio/sdk/plugin/autotrack/tmp/SubExample").isEqualTo(className);
        String superClassName = context.getSuperClassName();
        System.out.println(superClassName);
        Truth.assertThat("com/growingio/sdk/plugin/autotrack/tmp/SuperExample").isEqualTo(superClassName);
        boolean isAbstract = context.isAbstract();
        System.out.println(isAbstract);
        Truth.assertThat(isAbstract).isFalse();
        boolean isClassModified = context.isClassModified();
        System.out.println(isClassModified);
        Truth.assertThat(isClassModified).isFalse();
        Truth.assertThat(context.isAssignable("com/growingio/sdk/plugin/autotrack/tmp/SubExample", "com/growingio/sdk/plugin/autotrack/tmp/SuperExample")).isTrue();
    }
}
