package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.Model;
import mg.itu.prom16.annotations.Param;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class Mapping {
    String controller;
    String method;

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Mapping(String controller, String method) {
        this.controller = controller;
        this.method = method;
    }

    @SuppressWarnings("deprecation")
    public Object execMethod(HttpServletRequest req) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ServletException {
        try {
            Class<?> clazz = Class.forName(controller);
            Method[] methodes = clazz.getDeclaredMethods();
            Method oneMethod = null;

            for(Method meth : methodes) {
                if (meth.getName().equals(method))
                    oneMethod = meth;
            }
            Class<?>[] classes = oneMethod.getParameterTypes();

            try {
                assert oneMethod != null;
                Parameter[] parameters = oneMethod.getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i=0; i<parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(Param.class)) {
                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getAnnotation(Param.class).name()));
                    } else if (parameters[i].isAnnotationPresent(Model.class)) {

                        try {
                            arguments[i] = getMethodObjet(parameters[i], req);
                        } catch (Exception e) {
                            throw new ServletException(e.getMessage());
                        }
                    }
                    else {
                        arguments[i] = parse(classes[i] ,req.getParameter(parameters[i].getName()));
                    }
                }

                return oneMethod.invoke(clazz.newInstance(),arguments);
            } catch (Exception e) {
                throw new ServletException(e.getMessage());
            }


        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }

    }

    @SuppressWarnings("deprecation")
    Object getMethodObjet(Parameter parameter, HttpServletRequest req) throws ServletException {
        Class<?> classeParametre = parameter.getType();
        String prefix = parameter.getAnnotation(Model.class).value();
        Field[] fields = classeParametre.getDeclaredFields();
        Object objet = null;
        try {
            try { objet = classeParametre.newInstance(); } catch (Exception e) { throw new ServletException("Pas de constructeur par defaut"); }

            Map<String, String[]> parameterMap = req.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {

                Method setter = null;
                for(Field field: fields) {
                    if (( prefix + "." + field.getName()).equals(entry.getKey())) {
                        setter = searchMethod(classeParametre, "set" + capitalizeFirstLetter(field.getName()));
                    }
                }

                if (setter == null) {
//                    String all = "";
//                    for(Field field: fields) {
//                        all += field.getName() + ",";
//                    }
//                    all += classeParametre.getName();
//                    throw new ServletException(all);
//                    throw new ServletException("la methode " + "set" + capitalizeFirstLetter(fields[0].getName()) + "() n'existe pas dans la classe " + objet.getClass());
                }
                else
                    setter.invoke(objet, parse(setter.getParameterTypes()[0],entry.getValue()[0]));
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
        return objet;
    }

    public Object parse(Class<?> clazz, String value) {
        if (clazz.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (clazz.equals(String.class)) {
            return value;
        } else {
            return clazz.cast(value);
        }
    }

    Method searchMethod(Class<?> clazz, String methodName) {
        Method[] listeMethode = clazz.getDeclaredMethods();
        for(Method meth : listeMethode) {

            if (meth.getName().equals(methodName)) {
                return meth;
            }
        }

        return null;
    }

    public static String capitalizeFirstLetter(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
