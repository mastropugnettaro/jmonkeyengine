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
import com.jme3.gde.terraineditor.ExtraToolParams;
import com.jme3.gde.terraineditor.TerrainEditorController;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import org.openide.loaders.DataObject;

/**
 * Roughens the terrain using a fractal noise routine.
 * @author Brent Owens
 */
public class RoughTerrainTool extends TerrainTool {

    private RoughExtraToolParams params;
    private final TerrainEditorController editorController;


    public RoughTerrainTool(TerrainEditorController controller) {
        this.editorController = controller;
    }

    @Override
    public void actionPrimary(Vector3f point, int textureIndex, AbstractSceneExplorerNode rootNode, DataObject dataObject) {
        if (radius == 0 || weight == 0)
            return;
        RoughTerrainToolAction action = new RoughTerrainToolAction(editorController, point, radius, weight, (RoughExtraToolParams)params);
        action.doActionPerformed(rootNode, dataObject);
    }

    @Override
    public void actionSecondary(Vector3f point, int textureIndex, AbstractSceneExplorerNode rootNode, DataObject dataObject) {
        // do nothing
    }

    @Override
    public void addMarkerPrimary(Node parent) {
        super.addMarkerPrimary(parent);
        markerPrimary.getMaterial().setColor("Color", ColorRGBA.Yellow);
    }

    @Override
    public void setExtraParams(ExtraToolParams params) {
        if (params instanceof RoughExtraToolParams)
            this.params = (RoughExtraToolParams) params;
    }

    @Override
    public ExtraToolParams getExtraParams() {
        return params;
    }

    @Override
    public void extraParamsChanged(ExtraToolParams params) {
        if (params instanceof RoughExtraToolParams)
            this.params = (RoughExtraToolParams) params;
    }
}
