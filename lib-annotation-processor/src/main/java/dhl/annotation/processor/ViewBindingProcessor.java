package dhl.annotation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import dhl.annotation.viewbinding.BindClick;
import dhl.annotation.viewbinding.BindView;
import dhl.annotation.viewbinding.Const;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Created by DuanHl on 2017/9/25.
 */
@AutoService(Processor.class)
public class ViewBindingProcessor extends AbstractProcessor {

	private static final Diagnostic.Kind LOGI = Diagnostic.Kind.NOTE;
	private static final Diagnostic.Kind LOGW = Diagnostic.Kind.WARNING;

	private Filer mFiler;
	private Elements mElementUtils;
	private Messager mMessager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		mFiler = processingEnv.getFiler();
		mElementUtils = processingEnv.getElementUtils();
		mMessager = processingEnv.getMessager();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> set = new HashSet<>(2);
		set.add(BindView.class.getCanonicalName());
		set.add(BindClick.class.getCanonicalName());
		return set;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		printLog(LOGI, "");
		printLog(LOGI, "ViewBindingProcessor-------------start process----------------");
		Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(BindView.class);
		Map<String, List<Element>> fieldMap = processView(fields);

		Set<? extends Element> methods = roundEnv.getElementsAnnotatedWith(BindClick.class);
		Map<String, List<Element>> methodMap = processClick(methods);

		genJavaFile(fieldMap, methodMap);

		printLog(LOGI, "ViewBindingProcessor-------------end process----------------");
		printLog(LOGI, "");
		return false;
	}

	private Map<String, List<Element>> processView(Set<? extends Element> fields) {
		printLog(LOGI, "");
		printLog(LOGI, "ViewBindingProcessor-----annotated BindView field size:" + (fields == null ? 0 : fields.size()));
		if (fields == null || fields.isEmpty()) return null;
		Map<String, List<Element>> map = new HashMap<>();
		for (Element fieldElement : fields) {
			if (isFieldValid(fieldElement)) {
				PackageElement packageElement = mElementUtils.getPackageOf(fieldElement);
				TypeElement classElement = (TypeElement) fieldElement.getEnclosingElement();
				String className = packageElement.getQualifiedName().toString() + "." + classElement.getSimpleName().toString();
				List<Element> elements = map.get(className);
				if (elements == null) {
					elements = new ArrayList<>();
					map.put(className, elements);
				}
				elements.add(fieldElement);
			}
		}
		return map;
	}

	private Map<String, List<Element>> processClick(Set<? extends Element> methods) {
		printLog(LOGI, "");
		printLog(LOGI, "ViewBindingProcessor-----annotated BindClick method size:" + (methods == null ? 0 : methods.size()));
		if (methods == null || methods.isEmpty()) return null;
		Map<String, List<Element>> map = new HashMap<>();
		for (Element methodElement : methods) {
			if (isMethodValid(methodElement)) {
				PackageElement packageElement = mElementUtils.getPackageOf(methodElement);
				TypeElement classElement = (TypeElement) methodElement.getEnclosingElement();
				String className = packageElement.getQualifiedName().toString() + "." + classElement.getSimpleName().toString();
				List<Element> elements = map.get(className);
				if (elements == null) {
					elements = new ArrayList<>();
					map.put(className, elements);
				}
				elements.add(methodElement);
			}
		}
		return map;
	}

	private void genJavaFile(Map<String, List<Element>> fieldMap, Map<String, List<Element>> methodMap) {
		ClassName viewType = ClassName.get("android.view", "View");
		ClassName viewBinder = ClassName.get("dhl.viewbinding", "ViewBinder");
		ClassName clickListenerType = ClassName.get("android.view.View", "OnClickListener");

		if (fieldMap != null && fieldMap.size() > 0) {
			for (Map.Entry<String, List<Element>> entry : fieldMap.entrySet()) {
				if (entry.getValue() == null || entry.getValue().isEmpty()) continue;

				String packageName = entry.getKey().substring(0, entry.getKey().lastIndexOf('.'));
				String className = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
				ClassName target = ClassName.get(packageName, className);
				ParameterizedTypeName binderInterface = ParameterizedTypeName.get(viewBinder, target);

				List<Element> fields = entry.getValue();
				List<Element> methods = methodMap == null ? null : methodMap.remove(entry.getKey());

				MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
								.addAnnotation(Override.class)
								.addModifiers(Modifier.PUBLIC)
								.returns(void.class)
								.addParameter(target, "target", Modifier.FINAL)
								.addParameter(viewType, "finder", Modifier.FINAL);

				Map<Integer, Element> fid = new HashMap<>();
				if (fields != null && fields.size() > 0) {
					for (Element field : fields) {
						BindView anno = field.getAnnotation(BindView.class);
						int id = anno.value();

						String name = field.getSimpleName().toString();
						methodBuilder.addStatement("target.$L = ($T) finder.findViewById($L)", name, field.asType(), id);

						fid.put(id, field);
					}
				}

				if (methods != null && methods.size() > 0) {
					TypeSpec.Builder lisBuilder = TypeSpec.anonymousClassBuilder("")
									.addSuperinterface(clickListenerType);
					MethodSpec.Builder clickMethodBuilder = MethodSpec.methodBuilder("onClick")
									.addAnnotation(Override.class)
									.addModifiers(Modifier.PUBLIC)
									.returns(void.class)
									.addParameter(viewType, "view")
									.addCode("switch (view.getId()) {\n");

					for (Element method : methods) {
						BindClick anno = method.getAnnotation(BindClick.class);
						int[] ids = anno.value();

						for (int id : ids) {
							clickMethodBuilder.addCode("case $L:\n", id)
											.addStatement("target.$N(view)", method.getSimpleName())
											.addStatement("break");
						}
					}

					clickMethodBuilder.addStatement("}");

					lisBuilder.addMethod(clickMethodBuilder.build());

					methodBuilder.addStatement("$T lis = $L", clickListenerType, lisBuilder.build());

					for (Element method : methods) {
						BindClick anno = method.getAnnotation(BindClick.class);
						int[] ids = anno.value();

						for (int id : ids) {
							Element field = fid.get(id);
							if (field != null) {
								methodBuilder.addStatement("target.$N.setOnClickListener(lis)", field.getSimpleName().toString());
							} else {
								methodBuilder.addStatement("finder.findViewById($L).setOnClickListener(lis)", id);
							}
						}
					}
				}

				MethodSpec methodSpec = methodBuilder.build();

				TypeSpec classType = TypeSpec.classBuilder(className + Const.CLASS_SUFFIX)
								.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
								.addSuperinterface(binderInterface)
								.addMethod(methodSpec)
								.build();

				JavaFile javaFile = JavaFile.builder(packageName, classType).build();
				try {
					javaFile.writeTo(mFiler);
					printLog(LOGI, "ViewBindingProcessor----gen java file:" + classType.name);
				} catch (IOException e) {
					printLog(LOGW, "ViewBindingProcessor----gen java file fail!!!!!!:" + classType.name);
					e.printStackTrace();
				}
			}
		}

		if (methodMap != null && methodMap.size() > 0) {
			for (Map.Entry<String, List<Element>> entry : methodMap.entrySet()) {
				String packageName = entry.getKey().substring(0, entry.getKey().lastIndexOf('.'));
				String className = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
				ClassName target = ClassName.get(packageName, className);
				ParameterizedTypeName binderInterface = ParameterizedTypeName.get(viewBinder, target);

				List<Element> methods = entry.getValue();
				if (methods != null && methods.size() > 0) {
					MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
									.addAnnotation(Override.class)
									.addModifiers(Modifier.PUBLIC)
									.returns(void.class)
									.addParameter(target, "target", Modifier.FINAL)
									.addParameter(viewType, "finder", Modifier.FINAL);

					TypeSpec.Builder lisBuilder = TypeSpec.anonymousClassBuilder("")
									.addSuperinterface(clickListenerType);
					MethodSpec.Builder clickMethodBuilder = MethodSpec.methodBuilder("onClick")
									.addAnnotation(Override.class)
									.addModifiers(Modifier.PUBLIC)
									.returns(void.class)
									.addParameter(viewType, "view")
									.addCode("switch (view.getId()) {\n");

					for (Element method : methods) {
						BindClick anno = method.getAnnotation(BindClick.class);
						int[] ids = anno.value();

						for (int id : ids) {
							clickMethodBuilder.addCode("case $L:\n", id)
											.addStatement("target.$N(view)", method.getSimpleName())
											.addStatement("break");
						}
					}

					clickMethodBuilder.addStatement("}");

					lisBuilder.addMethod(clickMethodBuilder.build());

					methodBuilder.addStatement("$T lis = $L", clickListenerType, lisBuilder.build());

					for (Element method : methods) {
						BindClick anno = method.getAnnotation(BindClick.class);
						int[] ids = anno.value();

						for (int id : ids) {
							methodBuilder.addStatement("finder.findViewById($L).setOnClickListener(lis)", id);
						}
					}

					MethodSpec methodSpec = methodBuilder.build();

					TypeSpec classType = TypeSpec.classBuilder(className + Const.CLASS_SUFFIX)
									.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
									.addSuperinterface(binderInterface)
									.addMethod(methodSpec)
									.build();

					JavaFile javaFile = JavaFile.builder(packageName, classType).build();
					try {
						javaFile.writeTo(mFiler);
						printLog(LOGI, "ViewBindingProcessor----gen java file:" + classType.name);
					} catch (IOException e) {
						printLog(LOGW, "ViewBindingProcessor----gen java file fail!!!!!!:" + classType.name);
						e.printStackTrace();
					}

				}
			}
		}
	}

	private boolean isFieldValid(Element element) {

		if (element == null) {
			return false;
		}

		if (!(element instanceof VariableElement)) {
			printLog(LOGW, "ViewBindingProcessor----" + element.getSimpleName().toString() + " isn't a field");
			return false;
		}

		TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
		if (enclosingElement.getKind() != ElementKind.CLASS) {
			printLog(LOGW, "ViewBindingProcessor----" + enclosingElement.getSimpleName().toString() + " isn't class, field must belong a class!!!");
			return false;
		}

		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(PRIVATE)) {
			printLog(LOGW, "ViewBindingProcessor----" + enclosingElement.getSimpleName().toString() + " -> " + element.getSimpleName().toString() + " can't be private!!!!!");
			return false;
		}

		return true;
	}

	private boolean isMethodValid(Element element) {

		if (element == null) {
			return false;
		}

		if (!(element instanceof ExecutableElement)) {
			printLog(LOGW, "ViewBindingProcessor----" + element.getSimpleName().toString() + " isn't a method");
			return false;
		}

		TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
		if (enclosingElement.getKind() != ElementKind.CLASS) {
			printLog(LOGW, "ViewBindingProcessor----" + enclosingElement.getSimpleName().toString() + " isn't class, method must belong a class!!!");
			return false;
		}

		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(PRIVATE)) {
			printLog(LOGW, "ViewBindingProcessor----" + enclosingElement.getSimpleName().toString() + " -> " + element.getSimpleName().toString() + " can't be private!!!!!");
			return false;
		}

		return true;
	}

	private void printLog(Diagnostic.Kind kind, String msg) {
		mMessager.printMessage(kind, msg);
	}
}
