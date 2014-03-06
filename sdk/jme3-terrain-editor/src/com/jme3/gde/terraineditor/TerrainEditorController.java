/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.gde.terraineditor;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.gde.core.assets.AssetDataObject;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.gde.core.undoredo.AbstractUndoableSceneEdit;
import com.jme3.gde.core.undoredo.SceneUndoRedoManager;
import com.jme3.gde.core.util.TerrainUtils;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.ProgressMonitor;
import com.jme3.terrain.Terrain;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import jme3tools.converters.ImageToAwt;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Modifies the actual terrain in the scene.
 *
 * @author normenhansen, bowens
 */
@SuppressWarnings("unchecked")
public class TerrainEditorController implements NodeListener
{

    private JmeSpatial jmeRootNode;
    private JmeSpatial selectedSpat;
    protected Node terrainNode;
    protected Node rootNode;
    private AssetDataObject currentFileObject;
    private TerrainEditorTopComponent topComponent;

    private TerrainSaveCookie terrainSaveCookie = new TerrainSaveCookie();

    //private InstanceContent content;





    public TerrainEditorController(JmeSpatial jmeRootNode,
                                    AssetDataObject currentFileObject,
                                    TerrainEditorTopComponent topComponent)
    {
        this.jmeRootNode = jmeRootNode;
        rootNode = this.jmeRootNode.getLookup().lookup(Node.class);
        this.currentFileObject = currentFileObject;
        //this.content = content;
        terrainSaveCookie.rootNode = jmeRootNode;
        this.currentFileObject.setSaveCookie(terrainSaveCookie);
        this.topComponent = topComponent;
        this.jmeRootNode.addNodeListener(this);
    }

    public TerrainEditorTopComponent getTopComponent() { return this.topComponent; }
    public Node getRootNode() { return this.rootNode; }

    public FileObject getCurrentFileObject() {
        return currentFileObject.getPrimaryFile();
    }

    public DataObject getCurrentDataObject() {
        return currentFileObject;
    }

    public void setNeedsSave(boolean state)
    {
        if (state && !currentFileObject.isModified())
            currentFileObject.setModified(state);
        else if (!state && currentFileObject.isModified())
            currentFileObject.setModified(state);
    }

    protected void setSelectedSpat(JmeSpatial selectedSpat) {

        if (this.selectedSpat == selectedSpat) {
            return;
        }
        if (this.selectedSpat != null) {
            this.selectedSpat.removePropertyChangeListener(this);
            this.selectedSpat.removeNodeListener(this);
        }
        this.selectedSpat = selectedSpat;
        if (selectedSpat != null) {
            selectedSpat.addPropertyChangeListener(this);
            selectedSpat.addNodeListener(this);
        }
    }

    // set texture table via mouseOver ray
    private void setTextureTable(Node terrainNode)
    {
        TextureTableModel tableModel = (TextureTableModel) topComponent.getTextureTable().getModel();

        Terrain terrain = (Terrain)terrainNode;
        Material terrainMaterial = terrain.getMaterial();

        Collection<MatParam> params = terrainMaterial.getParams();

        for (MatParam param : params)
        {
            if (param instanceof MatParamTexture)
            {
                MatParamTexture textureParam = (MatParamTexture)param;

                String paramName = textureParam.getName();
                TerrainLightingAttribute tla = TerrainLightingAttribute.getAttribute(paramName);
                // String texturePathName = textureParam.getTextureValue().getName();

                if (tla == null)
                    continue;

                switch(tla)
                {
                    case DiffuseMap_1:
                    {
                        tableModel.setTexture(0, textureParam.getTextureValue());
                        break;
                    }
                }

                String breakPoint = "herpderp";
            }
        }

        return;
    }

    public void setTerrainFromCollision(CollisionResult result)
    {


        Geometry geometry = result.getGeometry();
        Node node = geometry.getParent();
        Node parent = node.getParent();

        if ((!(parent == null)) && parent instanceof Terrain)
        {
            if (parent.equals(terrainNode))
                return;

            if (this.terrainNode != null)
            {
                // remember the last selected texture before displaying the new terrain data
                String terrainName = terrainNode.getName();
                int selectedRow = topComponent.getTextureTable().getSelectedRow();
                topComponent.getTerrainTextureController().setSelectedTextureReminder(terrainName, selectedRow);
            }

            this.terrainNode = parent;
            topComponent.getTerrainTextureController().reinitTextureTable();

        }
        else
        {
            terrainNode = null;
        }
    }

    public Node getTerrain(Spatial root) {
        if (terrainNode != null)
            return terrainNode;

        if (root == null)
            root = rootNode;

        // is this the terrain?
        if (root instanceof Terrain && root instanceof Node) {
            terrainNode = (Node)root;
            return terrainNode;
        }

        if (root instanceof Node) {
            Node n = (Node) root;
            for (Spatial c : n.getChildren()) {
                if (c instanceof Node){
                    Node res = getTerrain(c);
                    if (res != null)
                        return res;
                }
            }
        }

        return terrainNode;
    }

    public JmeNode findJmeTerrain(JmeNode root) {
        if (root == null)
            root = (JmeNode) jmeRootNode;

        Node node = root.getLookup().lookup(Node.class);
        if (node != null && node instanceof Terrain && node instanceof Node) {
            return root;
        }

        if (node != null) {
            if (root.getChildren() != null) {
                for (org.openide.nodes.Node child : root.getChildren().getNodes() ) {
                    if (child instanceof JmeNode) {
                        JmeNode res = findJmeTerrain((JmeNode)child);
                        if (res != null)
                            return res;
                    }
                }
            }
        }

        return null;
    }



    /**
     * Perform the actual height modification on the terrain.
     * @param worldLoc the location in the world where the tool was activated
     * @param radius of the tool, terrain in this radius will be affected
     * @param heightFactor the amount to adjust the height by
     */
    public void doModifyTerrainHeight(Vector3f worldLoc, float radius, float heightFactor) {

        Terrain terrain = (Terrain) getTerrain(null);
        if (terrain == null)
            return;

        setNeedsSave(true);

        int radiusStepsX = (int) (radius / ((Node)terrain).getLocalScale().x);
        int radiusStepsZ = (int) (radius / ((Node)terrain).getLocalScale().z);

        float xStepAmount = ((Node)terrain).getLocalScale().x;
        float zStepAmount = ((Node)terrain).getLocalScale().z;

        List<Vector2f> locs = new ArrayList<Vector2f>();
        List<Float> heights = new ArrayList<Float>();

        for (int z=-radiusStepsZ; z<radiusStepsZ; z++) {
            for (int x=-radiusStepsZ; x<radiusStepsX; x++) {

                float locX = worldLoc.x + (x*xStepAmount);
                float locZ = worldLoc.z + (z*zStepAmount);

                // see if it is in the radius of the tool
                if (isInRadius(locX-worldLoc.x,locZ-worldLoc.z,radius)) {
                    // adjust height based on radius of the tool
                    float h = calculateHeight(radius, heightFactor, locX-worldLoc.x, locZ-worldLoc.z);
                    // increase the height
                    locs.add(new Vector2f(locX, locZ));
                    heights.add(h);
                }
            }
        }

        // do the actual height adjustment
        terrain.adjustHeight(locs, heights);

        ((Node)terrain).updateModelBound(); // or else we won't collide with it where we just edited

    }

    /**
     * See if the X,Y coordinate is in the radius of the circle. It is assumed
     * that the "grid" being tested is located at 0,0 and its dimensions are 2*radius.
     * @param x
     * @param z
     * @param radius
     * @return
     */
    private boolean isInRadius(float x, float y, float radius) {
        Vector2f point = new Vector2f(x,y);
        // return true if the distance is less than equal to the radius
        return Math.abs(point.length()) <= radius;
    }

    /**
     * Interpolate the height value based on its distance from the center (how far along
     * the radius it is).
     * The farther from the center, the less the height will be.
     * This produces a linear height falloff.
     * @param radius of the tool
     * @param heightFactor potential height value to be adjusted
     * @param x location
     * @param z location
     * @return the adjusted height value
     */
    private float calculateHeight(float radius, float heightFactor, float x, float z) {
        float val = calculateRadiusPercent(radius, x, z);
        return heightFactor * val;
    }

    private float calculateRadiusPercent(float radius, float x, float z) {
         // find percentage for each 'unit' in radius
        Vector2f point = new Vector2f(x,z);
        float val = Math.abs(point.length()) / radius;
        val = 1f - val;
        return val;
    }

    public void cleanup() {
        terrainNode = null;
        rootNode = null;
    }

    /**
     * pre-calculate the terrain's entropy values
     */
    public void generateEntropies(final ProgressMonitor progressMonitor) {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return;

            terrain.generateEntropy(progressMonitor);
        } else {
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    generateEntropies(progressMonitor);
                    return null;
                }
            });
        }
    }

    /**
     * Set the scale of a texture at the specified layer
     * Blocks on the OGL thread
     */
    public void setTextureScale(final int layer, final float scale) {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return;
            terrain.getMaterial().setFloat("DiffuseMap_"+layer+"_scale", scale);
            setNeedsSave(true);
        } else {
            try {
                SceneApplication.getApplication().enqueue(new Callable<Object>() {
                    public Object call() throws Exception {
                        setTextureScale(layer, scale);
                        return null;
                    }
                }).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    // blocks on GL thread until terrain is created
    public Terrain createTerrain(final Node parent,
                                final int totalSize,
                                final int patchSize,
                                final int alphaTextureSize,
                                final float[] heightmapData,
                                final String sceneName,
                                final JmeSpatial jmeNodeParent) throws IOException
    {
        try {
            Terrain terrain =
            SceneApplication.getApplication().enqueue(new Callable<Terrain>() {
                public Terrain call() throws Exception {
                    //return doCreateTerrain(parent, totalSize, patchSize, alphaTextureSize, heightmapData, sceneName, jmeNodeParent);
                    AddTerrainAction a = new AddTerrainAction();
                    return (Terrain) a.doCreateTerrain(parent, totalSize, patchSize, alphaTextureSize, heightmapData, sceneName, jmeRootNode);
                }
            }).get();
            return terrain;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null; // if failed
    }


    private void addSpatialUndo(final Node undoParent, final Spatial undoSpatial, final AbstractSceneExplorerNode parentNode) {
        //add undo
        if (undoParent != null && undoSpatial != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    //undo stuff here
                    undoSpatial.removeFromParent();
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    //redo stuff here
                    undoParent.attachChild(undoSpatial);
                }

                @Override
                public void awtRedo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }

                @Override
                public void awtUndo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }
            });
        }
    }

    /**
     * Save the terrain's alpha maps to disk, in the Textures/terrain-alpha/ directory
     * @throws IOException
     */


    public synchronized void doSaveAlphaImages(Terrain terr) {

        // terrainNode = null;
        // re-look it up
        Terrain terrain = (terr == null)
                ? (Terrain)getTerrain(rootNode)
                : terr;


        AssetManager manager = SceneApplication.getApplication().getAssetManager();
        String assetFolder = null;
        if (manager != null && manager instanceof ProjectAssetManager)
            assetFolder = ((ProjectAssetManager)manager).getAssetFolderName();

        if (assetFolder == null)
            throw new IllegalStateException("AssetManager was not a ProjectAssetManager. Could not locate image save directories.");

        Texture alpha1 = topComponent.getTerrainTextureController().doGetAlphaTexture(terrain, 0);
        BufferedImage bi1 = ImageToAwt.convert(alpha1.getImage(), false, true, 0);
        File imageFile1 = new File(assetFolder+"/"+alpha1.getKey().getName());

        Texture alpha2 = topComponent.getTerrainTextureController().doGetAlphaTexture(terrain, 1);
        BufferedImage bi2 = ImageToAwt.convert(alpha2.getImage(), false, true, 0);
        File imageFile2 = new File(assetFolder+"/"+alpha2.getKey().getName());

        Texture alpha3 = topComponent.getTerrainTextureController().doGetAlphaTexture(terrain, 2);
        BufferedImage bi3 = ImageToAwt.convert(alpha3.getImage(), false, true, 0);
        File imageFile3 = new File(assetFolder+"/"+alpha3.getKey().getName());

        ImageOutputStream ios1 = null;
        ImageOutputStream ios2 = null;
        ImageOutputStream ios3 = null;

        try
        {
            ios1 = new FileImageOutputStream(imageFile1);
            ios2 = new FileImageOutputStream(imageFile2);
            ios3 = new FileImageOutputStream(imageFile3);

            ImageIO.write(bi1, "png", ios1);
            ImageIO.write(bi2, "png", ios2);
            ImageIO.write(bi3, "png", ios3);
        }
        catch (IOException ex)
        {
            System.out.println("Failed saving alphamaps");
            System.out.println("    " + imageFile1);
            System.out.println("    " + imageFile2);
            System.out.println("    " + imageFile3);
            Exceptions.printStackTrace(ex);
        }
        finally
        {
            try
            {
                if (ios1 != null)
                    ios1.close();

                if (ios2 != null)
                    ios2.close();

                if (ios3 != null)
                    ios3.close();
            }
            catch (IOException ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    /**
     * Create a skybox with 6 textures.
     * Blocking call.
     */
    protected Spatial createSky(final Node parent,
                                final Texture west,
                                final Texture east,
                                final Texture north,
                                final Texture south,
                                final Texture top,
                                final Texture bottom,
                                final Vector3f normalScale)
    {
        try {
            Spatial sky =
            SceneApplication.getApplication().enqueue(new Callable<Spatial>() {
                public Spatial call() throws Exception {
                    return doCreateSky(parent, west, east, north, south, top, bottom, normalScale);
                }
            }).get();
            return sky;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null; // if failed
    }

    private Spatial doCreateSky(Node parent,
                                Texture west,
                                Texture east,
                                Texture north,
                                Texture south,
                                Texture top,
                                Texture bottom,
                                Vector3f normalScale)
    {
        AssetManager manager = SceneApplication.getApplication().getAssetManager();
        Spatial sky = SkyFactory.createSky(manager, west, east, north, south, top, bottom, normalScale);
        parent.attachChild(sky);
        return sky;
    }

    /**
     * Create a skybox with a single texture.
     * Blocking call.
     */
    protected Spatial createSky(final Node parent,
                                final Texture texture,
                                final boolean useSpheremap,
                                final Vector3f normalScale)
    {
        try {
            Spatial sky =
            SceneApplication.getApplication().enqueue(new Callable<Spatial>() {
                public Spatial call() throws Exception {
                    return doCreateSky(parent, texture, useSpheremap, normalScale);
                }
            }).get();
            return sky;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null; // if failed
    }

    private Spatial doCreateSky(Node parent,
                                Texture texture,
                                boolean useSpheremap,
                                Vector3f normalScale)
    {
        AssetManager manager = SceneApplication.getApplication().getAssetManager();
        Spatial sky = SkyFactory.createSky(manager, texture, normalScale, useSpheremap);
        parent.attachChild(sky);
        return sky;
    }

    public boolean isTriPlanarEnabled() {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return false;
            MatParam param = terrain.getMaterial().getParam("useTriPlanarMapping");
            if (param != null)
                return (Boolean)param.getValue();

            return false;
        } else {
            try {
                Boolean isEnabled =
                SceneApplication.getApplication().enqueue(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return isTriPlanarEnabled();
                    }
                }).get();
                return isEnabled;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }
    }

    /**
     * Also adjusts the scale. Normal texture scale uses texture coordinates,
     * which are each 1/(total size of the terrain). But for tri planar it doesn't
     * use texture coordinates, so we need to re-calculate it to be the same scale.
     * @param enabled
     * @param terrainTotalSize
     */
    public void setTriPlanarEnabled(final boolean enabled) {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return;
            terrain.getMaterial().setBoolean("useTriPlanarMapping", enabled);

            float size = terrain.getTerrainSize();

            if (enabled) {
                for (int i=0; i < topComponent.getTerrainTextureController().getNumUsedTextures(); i++) {
                    float scale = 1f/(float)(size / topComponent.getTerrainTextureController().getTextureScale(i));
                    setTextureScale(i, scale);
                }
            } else {
                for (int i=0; i< topComponent.getTerrainTextureController().getNumUsedTextures(); i++) {
                    float scale = (float)(size * topComponent.getTerrainTextureController().getTextureScale(i));
                    setTextureScale(i, scale);
                }
            }

            setNeedsSave(true);
        } else {
            try {
                SceneApplication.getApplication().enqueue(new Callable<Object>() {
                    public Object call() throws Exception {
                        setTriPlanarEnabled(enabled);
                        return null;
                    }
                }).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

     protected void setShininess(final float shininess) {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return;

            terrain.getMaterial().setFloat("Shininess", shininess);

            setNeedsSave(true);
        } else {
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    setShininess(shininess);
                    return null;
                }
            });
        }
    }

      protected float getShininess() {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return 0;

            MatParam param = terrain.getMaterial().getParam("Shininess");
            if (param != null)
                return (Float)param.getValue();

                return 0;
        } else {
            try {
                Float shininess = SceneApplication.getApplication().enqueue(new Callable<Float>() {

                    public Float call() throws Exception {
                        return getShininess();
                    }
                }).get();
                return shininess;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return 0;
        }
    }

    protected void setWardIsoEnabled(final boolean enabled) {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return;
            terrain.getMaterial().setBoolean("WardIso", enabled);

            setNeedsSave(true);
        } else {
            try {
                SceneApplication.getApplication().enqueue(new Callable<Object>() {
                    public Object call() throws Exception {
                        setWardIsoEnabled(enabled);
                        return null;
                    }
                }).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    protected boolean isWardIsoEnabled() {
        if (SceneApplication.getApplication().isOgl()) {
            Terrain terrain = (Terrain) getTerrain(null);
            if (terrain == null)
                return false;
            MatParam param = terrain.getMaterial().getParam("WardIso");
            if (param != null)
                return (Boolean)param.getValue();

            return false;
        } else {
            try {
                Boolean isEnabled =
                SceneApplication.getApplication().enqueue(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return isWardIsoEnabled();
                    }
                }).get();
                return isEnabled;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }
    }


    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getNewValue() == null && ev.getOldValue() != null) {
            topComponent.getTerrainTextureController().clearTextureTable(); // terrain deleted
            terrainNode = null;
        }
    }

    public void childrenAdded(NodeMemberEvent ev) {
        boolean isTerrain = false;
        for(org.openide.nodes.Node n : ev.getSnapshot()) {
            Node node = n.getLookup().lookup(Node.class);
            if (node instanceof Terrain) {
                isTerrain = true;
                break;
            }
        }
        if (isTerrain)
            topComponent.getTerrainTextureController().reinitTextureTable();
    }

    public void childrenRemoved(NodeMemberEvent ev) {

    }

    public void childrenReordered(NodeReorderEvent ev) {
    }

    public void nodeDestroyed(NodeEvent ev) {

    }

    /**
     * Re-attach the camera to the LOD control.
     * Called when the scene is opened and will only
     * update the control if there is already a terrain present in
     * the scene.
     */
    protected void setTerrainLodCamera() {
        Camera camera = SceneApplication.getApplication().getCamera();
        Node root = jmeRootNode.getLookup().lookup(Node.class);
        TerrainUtils.enableLodControl(camera, root);
    }

    class TerrainSaveCookie implements SaveCookie
    {
        JmeSpatial rootNode;

        public void save() throws IOException
        {
            if (topComponent.getTerrainTextureController().alphaLayersChanged)
            {
                SceneApplication.getApplication().enqueue(new Callable<Object>()
                {
                    public Object call() throws Exception
                    {
                        //currentFileObject.saveAsset();
                        //TerrainSaveCookie sc = currentFileObject.getCookie(TerrainSaveCookie.class);
                        //if (sc != null) {
                            //Node root = rootNode.getLookup().lookup(Node.class);
                            doSaveAlphaImages(null);
                            //content.remove(TerrainSaveCookie.this);
                        //}
                        return null;
                    }
                });

                topComponent.getTerrainTextureController().alphaLayersChanged = false;
            }
        }
    }

}
