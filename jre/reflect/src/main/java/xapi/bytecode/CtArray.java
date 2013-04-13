package xapi.bytecode;

import xapi.source.Modifier;


final class CtArray extends CtClass {
    protected ClassPool pool;

    // the name of array type ends with "[]".
    CtArray(String name, ClassPool cp) {
        super(name);
        pool = cp;
    }

    @Override
    public ClassPool getClassPool() {
        return pool;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    private CtClass[] interfaces = null;

    @Override
    public int getModifiers() {
        int mod = Modifier.FINAL;
        try {
            mod |= getComponentType().getModifiers()
                   & (Modifier.PROTECTED | Modifier.PUBLIC | Modifier.PRIVATE);
        }
        catch (NotFoundException e) {}
        return mod;
    }

    @Override
    public CtClass[] getInterfaces() throws NotFoundException {
        if (interfaces == null)
            interfaces = new CtClass[] {
                pool.get("java.lang.Cloneable"), pool.get("java.io.Serializable") };

        return interfaces;
    }

    @Override
    public boolean subtypeOf(CtClass clazz) throws NotFoundException {
        if (super.subtypeOf(clazz))
            return true;

        String cname = clazz.getName();
        if (cname.equals(javaLangObject)
            || cname.equals("java.lang.Cloneable")
            || cname.equals("java.io.Serializable"))
            return true;

        return clazz.isArray()
            && getComponentType().subtypeOf(clazz.getComponentType());
    }

    @Override
    public CtClass getComponentType() throws NotFoundException {
        String name = getName();
        return pool.get(name.substring(0, name.length() - 2));
    }

    @Override
    public CtClass getSuperclass() throws NotFoundException {
        return pool.get(javaLangObject);
    }

//    @Override
//    public CtMethod[] getMethods() {
//        try {
//            return getSuperclass().getMethods();
//        }
//        catch (NotFoundException e) {
//            return super.getMethods();
//        }
//    }
//
//    @Override
//    public CtMethod getMethod(String name, String desc)
//        throws NotFoundException
//    {
//        return getSuperclass().getMethod(name, desc);
//    }
//
//    @Override
//    public CtConstructor[] getConstructors() {
//        try {
//            return getSuperclass().getConstructors();
//        }
//        catch (NotFoundException e) {
//            return super.getConstructors();
//        }
//    }
}
