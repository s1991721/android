package com.ljf.apt_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by mr.lin on 2019/3/6
 */
public class ClassCreatorProxy {

    private String mBindingClassName;
    private String mPackageName;
    private TypeElement mTypeElement;
    private Map<Integer, VariableElement> mVariableElementMap = new HashMap<>();

    public ClassCreatorProxy(Elements elementUtils, TypeElement classElement) {
        this.mTypeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(mTypeElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = mTypeElement.getSimpleName().toString();
        this.mPackageName = packageName;
        this.mBindingClassName = className + "_ViewBinding";
    }

    public void putElement(int id, VariableElement element) {
        mVariableElementMap.put(id, element);
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();

        builder.append("package ").append(mPackageName).append(";\n");
        builder.append('\n');
        builder.append("public class ").append(mBindingClassName);
        builder.append(" {\n");
        generateMethods(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        builder.append("public void bind(" + mTypeElement.getQualifiedName() + " host) {\n");
        for (int id : mVariableElementMap.keySet()) {
            VariableElement element = mVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            builder.append("(" + type + ")(host.findViewById(" + id + "));\n");
        }
        builder.append("  }\n");
    }

    public String getProxyClassFullName() {
        return mPackageName + "." + mBindingClassName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public TypeSpec generateJavaCode2() {
        TypeSpec bindingClass = TypeSpec.classBuilder(mBindingClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethods2())
                .build();
        return bindingClass;
    }

    private MethodSpec generateMethods2() {
        ClassName host = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(host, "host");

        for (int id : mVariableElementMap.keySet()) {
            VariableElement element = mVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            methodBuilder.addCode("host." + name + " = " + "(" + type + ")" + "host.findViewById(" + id + ");\n");
        }

        return methodBuilder.build();
    }

    public String getPackageName() {
        return mPackageName;
    }
}
