import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

public class CopyUtil {
    // StackOverFlow protection
    private static final Map<Object, Object> copyObject = new IdentityHashMap<>();

    public static Object deepCopy(Object object) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object copy = null;

        //List copying
        if (object instanceof List<?>) {
            List<Object> result = new ArrayList<>();
            try {
                for (Object o : ((List<?>) object)) {
                    if (copyObject.containsKey(object)) { // Check for cyclic dependencies
                        result.add(copyObject.get(object));
                        copyObject.remove(object);
                        break;
                    } else
                        result.add(deepCopy(o));
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Map copying
        else if (object instanceof Map) {
            Map result = new HashMap<>();
            for (Entry entry : ((Map<?, Object>) object).entrySet()) {
                if (copyObject.containsKey(entry)) {
                    result.put(entry.getKey(), copyObject.get(entry));
                    break;
                } else
                    result.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return result;

            //String copying
        } else if (object instanceof String) {
            return object;

            //Arrays copying
        } else if (object.getClass().isArray()) {

            //String array copying
            if (object instanceof String[]) {
                String[] arr = (String[]) object;
                return Arrays.copyOf(arr, arr.length);
            }

            //Object array copying
            else if (object instanceof Object[]) {
                Object[] arr = (Object[]) object;
                Object[] result = new Object[arr.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = deepCopy(arr[i]);
                }
                return result;

                //Primitive array copying
            } else if (object instanceof int[]) {
                int[] arr = (int[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof byte[]) {
                byte[] arr = (byte[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof long[]) {
                long[] arr = (long[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof short[]) {
                short[] arr = (short[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof float[]) {
                float[] arr = (float[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof double[]) {
                double[] arr = (double[]) object;
                return Arrays.copyOf(arr, arr.length);
            } else if (object instanceof char[]) {
                char[] arr = (char[]) object;
                return Arrays.copyOf(arr, arr.length);
            }
            else if (object instanceof boolean[]) {
                boolean[] arr = (boolean[]) object;
                return Arrays.copyOf(arr, arr.length);
            }
        }

        // Complex object copying
        else {

            //Get object constructor
            Constructor<?>[] c = object.getClass().getConstructors();
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c.length - 1; j++) {
                    if (c[j].getGenericParameterTypes().length > c[j + 1].getGenericParameterTypes().length) {
                        Constructor<?> temp = c[j + 1];
                        c[j + 1] = c[j];
                        c[j] = temp;
                    }
                }
            }

            //Get params for constructor
            List<Object> list = new ArrayList<>();
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                for (int i = 0; i < c[0].getParameterTypes().length; i++) {
                    if (f.getType().getTypeName().equals(c[0].getParameterTypes()[i].getTypeName())) {
                        list.add(i, f.get(object));
                    }
                }
            }

            //Init new object from constructor
            copy = c[0].newInstance(list.toArray());

            //Copying object loop
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (!f.getType().isPrimitive() && !f.getType().isAssignableFrom(String.class)) {
                    if (copyObject.containsKey(f.get(object))) // Check for cyclic dependencies
                        f.set(copy, copyObject.get(f.get(object)));
                    else {
                        copyObject.put(f.get(object), f.get(copy));
                        f.set(copy, deepCopy(f.get(object)));
                    }
                } else {
                    if (f.get(object).equals(object))
                        f.set(copy, object);
                    f.set(copy, f.get(object));
                }
            }
        }
        copyObject.remove(object);
        return copy;
    }
}
