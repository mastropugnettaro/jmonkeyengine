package com.g3d.material;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.shader.DefineList;
import com.g3d.shader.UniformBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechniqueDef implements Savable {

    public enum LightMode {
        Disable,
        SinglePass,
        MultiPass,
        FixedPipeline,
    }

    public enum ShadowMode {
        Disable,
        InPass,
        PostPass,
    }

    private String name;

    private String vertName;
    private String fragName;
    private String shaderLang;
    private DefineList presetDefines;;

    private RenderState renderState;
    private LightMode lightMode   = LightMode.Disable;
    private ShadowMode shadowMode = ShadowMode.Disable;

    private Map<String, String> defineParams;
    private List<UniformBinding> worldBinds;
//    private final Map<String, Attribute> attribs = new HashMap<String, Attribute>();

    public TechniqueDef(String name){
        this.name = name == null ? "Default" : name;
        worldBinds = new ArrayList<UniformBinding>();
        presetDefines = new DefineList();
        defineParams = new HashMap<String, String>();
    }

    /**
     * Do not use this constructor.
     */
    public TechniqueDef(){
    }

    public void write(G3DExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
        oc.write(vertName, "vertName", null);
        oc.write(fragName, "fragName", null);
        oc.write(shaderLang, "shaderLang", null);
        oc.write(presetDefines, "presetDefines", null);
        oc.write(lightMode, "lightMode", LightMode.Disable);
        oc.write(shadowMode, "shadowMode", ShadowMode.Disable);
        oc.write(renderState, "renderState", null);
        // TODO: Finish this when Map<String, String> export is available
//        oc.writeS(defineParams, "defineParams", null);
        // TODO: Finish this when List<Enum> export is available
//        oc.write(worldBinds, "worldBinds", null);
    }

    public void read(G3DImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        vertName = ic.readString("vertName", null);
        fragName = ic.readString("fragName", null);
        shaderLang = ic.readString("shaderLang", null);
        presetDefines = (DefineList) ic.readSavable("presetDefines", null);
        lightMode = ic.readEnum("lightMode", LightMode.class, LightMode.Disable);
        shadowMode = ic.readEnum("shadowMode", ShadowMode.class, ShadowMode.Disable);
        renderState = (RenderState) ic.readSavable("renderState", null);
    }

    public String getName(){
        return name;
    }

    public LightMode getLightMode() {
        return lightMode;
    }

    public void setLightMode(LightMode lightMode) {
        this.lightMode = lightMode;
    }

    public ShadowMode getShadowMode() {
        return shadowMode;
    }

    public void setShadowMode(ShadowMode shadowMode) {
        this.shadowMode = shadowMode;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

    public void setShaderFile(String vert, String frag, String lang){
        this.vertName = vert;
        this.fragName = frag;
        this.shaderLang = lang;
    }

    public DefineList getShaderPresetDefines() {
        return presetDefines;
    }

    public String getShaderParamDefine(String paramName){
        return defineParams.get(paramName);
    }

    public void addShaderParamDefine(String paramName, String defineName){
        defineParams.put(paramName, defineName);
    }

    public void addShaderPresetDefine(String defineName, String value){
        presetDefines.set(defineName, value);
    }

    public String getFragName() {
        return fragName;
    }

    public String getVertName() {
        return vertName;
    }

    public String getShaderLanguage() {
        return shaderLang;
    }

    public boolean addWorldParam(String name) {
        for (UniformBinding binding : UniformBinding.values()) {
            if (binding.name().equals(name)) {
                worldBinds.add(binding);
                return true;
            }
        }
        return false;
    }

//    public void addAttribute(String name) {
//        Attribute attrib = shader.getAttribute(name);
//        attribs.put(name, attrib);
//    }

    public List<UniformBinding> getWorldBindings() {
        return worldBinds;
    }

}
