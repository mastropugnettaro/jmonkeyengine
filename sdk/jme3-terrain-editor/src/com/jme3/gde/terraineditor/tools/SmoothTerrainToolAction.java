/*
 * Copyright (c) 2009-2011 jMonkeyEngine
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
package com.jme3.gde.terraineditor.tools;

import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.terraineditor.TerrainEditorController;
import com.jme3.gde.terraineditor.tools.TerrainTool.Meshes;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.terrain.Terrain;
import java.util.ArrayList;
import java.util.List;

/**
 * Smooth bumps in the terrain by averaging the height in the tool radius.
 * The smoothAmount affects how many neighbour points are averaged, The smaller
 * the value, then only the smaller bumps will disappear. A large value will
 * smooth larger hills.
 *
 * @author sploreg
 */
public class SmoothTerrainToolAction extends AbstractTerrainToolAction {

    private Vector3f worldLoc;
    private float radius;
    private float height;

    List<Vector2f> undoLocs;
    List<Float> undoHeights;
    private final Meshes mesh;

    private final TerrainEditorController editorController;

    public SmoothTerrainToolAction(TerrainEditorController controller, Vector3f markerLocation, float radius, float height, Meshes mesh) {
        this.editorController = controller;
        this.worldLoc = markerLocation.clone();
        this.radius = radius;
        this.height = height;
        this.mesh = mesh;
        name = "Smooth terrain";
    }

    @Override
    protected Object doApplyTool(AbstractSceneExplorerNode rootNode) {
        // Terrain terrain = getTerrain(rootNode.getLookup().lookup(Node.class));
        Terrain terrain = (Terrain)editorController.getTerrain(null);
        if (terrain == null)
            return null;
        modifyHeight(terrain, worldLoc, radius, height, mesh);
        return terrain;
    }

    @Override
    protected void doUndoTool(AbstractSceneExplorerNode rootNode, Object undoObject) {
        if (undoObject == null)
            return;
        if (undoLocs == null || undoHeights == null)
            return;
        resetHeight((Terrain)undoObject, undoLocs, undoHeights);
    }

    private void modifyHeight(Terrain terrain, Vector3f worldLoc, float radius, float height, Meshes mesh) {

        int radiusStepsX = (int)(radius / ((Node)terrain).getLocalScale().x);
        int radiusStepsZ = (int)(radius / ((Node)terrain).getLocalScale().z);

        float xStepAmount = ((Node)terrain).getLocalScale().x;
        float zStepAmount = ((Node)terrain).getLocalScale().z;

        List<Vector2f> locs = new ArrayList<Vector2f>();
        List<Float> heights = new ArrayList<Float>();

        // Calculate the center of the terrain that the mouse is hovering over, instead of assuming
        // a singular position of Vector3f.ZERO
        Vector3f terrainPos = ((Node)terrain).getLocalTranslation();
        Vector3f workPos = worldLoc.subtractLocal(terrainPos);

        for (int z=-radiusStepsZ; z<radiusStepsZ; z++) {
            for (int x=-radiusStepsX; x<radiusStepsX; x++) {

                float locX = workPos.x + (x*xStepAmount);
                float locZ = workPos.z + (z*zStepAmount);

                // see if it is in the radius of the tool
                if (ToolUtils.isInMesh(locX-workPos.x,locZ-workPos.z,radius,mesh)) {

                    Vector2f terrainLoc = new Vector2f(locX, locZ);
                    // adjust height based on radius of the tool
                    float center = terrain.getHeightmapHeight(terrainLoc);
                    float left = terrain.getHeightmapHeight(new Vector2f(terrainLoc.x-1, terrainLoc.y));
                    float right = terrain.getHeightmapHeight(new Vector2f(terrainLoc.x+1, terrainLoc.y));
                    float up = terrain.getHeightmapHeight(new Vector2f(terrainLoc.x, terrainLoc.y+1));
                    float down = terrain.getHeightmapHeight(new Vector2f(terrainLoc.x, terrainLoc.y-1));
                    int count = 1;
                    float amount = center;
                    if ( !isNaN(left) ) {
                        amount += left;
                        count++;
                    }
                    if ( !isNaN(right) ) {
                        amount += right;
                        count++;
                    }
                    if ( !isNaN(up) ) {
                        amount += up;
                        count++;
                    }
                    if ( !isNaN(down) ) {
                        amount += down;
                        count++;
                    }

                    amount /= count; // take average

                    // weigh it
                    float diff = amount-center;
                    diff *= height;

                    locs.add(terrainLoc);
                    heights.add(diff);
                }
            }
        }

        undoLocs = locs;
        undoHeights = heights;

        // do the actual height adjustment
        terrain.adjustHeight(locs, heights);

        ((Node)terrain).updateModelBound(); // or else we won't collide with it where we just edited
    }

    private boolean isNaN(float val) {
        return val != val;
    }

    private void resetHeight(Terrain terrain, List<Vector2f> undoLocs, List<Float> undoHeights) {
        List<Float> neg = new ArrayList<Float>();
        for (Float f : undoHeights)
            neg.add( f * -1f );

        terrain.adjustHeight(undoLocs, neg);
        ((Node)terrain).updateModelBound();
    }
}
