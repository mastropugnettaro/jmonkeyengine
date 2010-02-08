package com.g3d.bounding;

import com.g3d.bounding.BoundingVolume.Type;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Camera.FrustumIntersect;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Spatial;
import com.g3d.scene.VertexBuffer;
import com.g3d.util.TempVars;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>Octree</code> provides an Octree implementation using TriangleBatch 
 * as triangle data source.<br>
 * There is also some intersection methods to find the desired Octree Node.
 * 
 * @author Lucas Goraieb
 */
public class Octree {

    private BoundingBox boundingBox;
    private List<Octree> children = new ArrayList<Octree>();
    private List<Integer> triangleData;

    private Spatial meshobject;
    private Mesh mesh;
    //private int[] indices;
    private int trianglePerNode;
	
   /**
     * Octree's public constructor. Use the trianglePerNode parameter to fine tune
     * the number of nodes according to the size or triangle count of your mesh.
     * @param mesh Spatial node that defines it's world translation/rotation
     * @param batch TriangleBatch as triangle data source
     * @param trianglePerNode Max number of triangles in a node
     */
    public Octree(Geometry geom, int trianglePerNode) {
        this.meshobject = geom;
        this.mesh = geom.getMesh();
        this.trianglePerNode = trianglePerNode;

        BoundingBox bb = new BoundingBox();
        ArrayList<Mesh> meshdata = new ArrayList<Mesh>();
        meshdata.add(mesh);

        //bb.computeFromBatches(meshdata);
        int[] indices = new int[mesh.getTriangleCount()];
        bb.computeFromPoints((FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData());
//        bb.computeFromTris(mesh.getTriangleIndices(indices),
//                           meshdata.get(0),
//                           0,
//                           meshdata.get(0).getTriangleCount());

        //Transform and scale to node's coordinates
        float largeSize = bb.xExtent;
        if (bb.yExtent > largeSize) {
            largeSize = bb.yExtent;
        }
        if (bb.zExtent > largeSize) {
            largeSize = bb.zExtent;
        }

        Vector3f scaled = geom.getWorldScale().mult(largeSize);
        largeSize = scaled.x;
        if (scaled.y > largeSize) {
            largeSize = scaled.y;
        }
        if (scaled.z > largeSize) {
            largeSize = scaled.z;
        }

        this.boundingBox = new BoundingBox(geom.localToWorld(bb.getCenter(), new Vector3f()),
                                           largeSize, largeSize, largeSize);

        this.triangleData = new ArrayList<Integer>();
        for (int i = 0; i < mesh.getTriangleCount(); i++) {
            this.triangleData.add(i);
        }
    }
    
    /**
     * Octree's public construtor. Use the trianglePerNode parameter to fine tune
     * the number of nodes according to the size or triangle count of your mesh.
     * @param mesh Spatial node that defines it's world translation/rotation
     * @param batch TriangleBatch as triangle data source
     * @param trianglePerNode Max number of triangles in a node
     */
//    public Octree(TriMesh mesh, int trianglePerNode) {
//        this.mesh = mesh;
//        this.trianglePerNode = trianglePerNode;
//        BoundingBox bb = new BoundingBox();
//        bb.computeFromPoints(mesh.getVertexBuffer());
//
//        //Transform and scale to node's coordinates
//        float largeSize = bb.xExtent;
//        if (bb.yExtent > largeSize) {
//            largeSize = bb.yExtent;
//        }
//        if (bb.zExtent > largeSize) {
//            largeSize = bb.zExtent;
//        }
//        Vector3f scaled = mesh.getWorldScale().mult(largeSize);
//        largeSize = scaled.x;
//        if (scaled.y > largeSize) {
//            largeSize = scaled.y;
//        }
//        if (scaled.z > largeSize) {
//            largeSize = scaled.z;
//        }
//        this.boundingBox = new BoundingBox(mesh.localToWorld(bb.getCenter(), new Vector3f()), largeSize, largeSize, largeSize);
//
//        this.triangleData = new ArrayList<Integer>();
//        for (int i = 0; i < mesh.getTriangleCount(); i++) {
//            this.triangleData.add(i);
//        }
//
//    }

    /*
     * Protected octree construtor, for the octree's children.
     */
    protected Octree(Octree parent, BoundingBox bb, List<Integer> tris) {
        this.boundingBox = bb;
        this.trianglePerNode = parent.getTrianglePerNode();
        this.mesh = parent.mesh;
        this.triangleData = tris;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public int getTrianglePerNode() {
        return trianglePerNode;
    }

    /**
     * Get node's bounding box
     * @return node's bounding box
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    protected List<Integer> getTriangleData() {
        return triangleData;
    }

    /*
     * Check every triangle for intersection with the Bounding Box.
     */
    private List<Integer> collectTriangles(BoundingBox bb) {
        assert TempVars.get().lock();
        Triangle tempTri = TempVars.get().triangle;

        List<Integer> triList = new ArrayList<Integer>();
        for (Integer i : triangleData) {
            mesh.getTriangle(i, tempTri);

            Vector3f v1 = tempTri.get(0);
            Vector3f v2 = tempTri.get(1);
            Vector3f v3 = tempTri.get(2);

            //Transform, rotate and scale to node's coordinates
            meshobject.localToWorld(v1, v1);
            meshobject.localToWorld(v2, v2);
            meshobject.localToWorld(v3, v3);
            if (bb.contains(v1) || bb.contains(v2) || bb.contains(v3)) {
                triList.add(i);
            }
        }
        assert TempVars.get().unlock();

        return triList;
    }

    /**
     * Build the octree. Required, otherwise the octree will be empty.
     */
    public void build() {
        if (triangleData.size() > trianglePerNode) {
            subdivide();
            /* To avoid recusive use of memory, try to free this unused data
             (the node children already have the needed info) */
            triangleData = null;
            System.gc();
            for (Octree child : children) {
                child.build();
            }
        } else {
            onRecurseFinished();
        }
    }

    /*
     * Creates one Child Node based on x,y,z coordinates
     */
    private void newNode(int x, int y, int z) {
        float extent = boundingBox.xExtent / 2;
        Vector3f center = new Vector3f();
        center.x = boundingBox.getCenter().x + (extent * x);
        center.y = boundingBox.getCenter().y + (extent * y);
        center.z = boundingBox.getCenter().z + (extent * z);
        BoundingBox bb = new BoundingBox(center, extent, extent, extent);
        List<Integer> tris = collectTriangles(bb);
        if (tris.size() > 0) {
            children.add(newOctreeNode(bb, tris));
        }
    }

    /*
     * Subdivide the Octree node into 8 or less
     */
    private void subdivide() {
        onSubdivide();
        newNode(-1, -1, -1);
        newNode(1, -1, -1);
        newNode(-1, 1, -1);
        newNode(1, 1, -1);
        newNode(-1, -1, 1);
        newNode(1, -1, 1);
        newNode(-1, 1, 1);
        newNode(1, 1, 1);
    }

    /*
     * Collects the intersected triangle data 
     */
    private boolean intersectHit(Set<Integer> tris) {
        onIntersectHit();
        if (triangleData != null) {
            tris.addAll(triangleData);
            return true;
        } else {
            return false;
        }
    }

    public Set<Integer> intersect(BoundingVolume bv) {
        if (bv.getType() == Type.AABB) {
            return intersectBox((BoundingBox) bv);
        } else if (bv.getType() == Type.Sphere) {
            return intersectSphere((BoundingSphere) bv);
//        } else if (bv.getType() == Type.Capsule) {
//            return intersectCapsule((BoundingCapsule) bv);
        } else {
            return intersectPoint(bv.getCenter());
        }
    }

    /**
     * Intersect a point with the Octree
     * @param point
     * @return List of triangle indexes to be used with TriangleBatch.getTriangle
     */
    public Set<Integer> intersectPoint(Vector3f point) {
        onIntersectStart();
        Set<Integer> tris = new HashSet<Integer>();
        if (boundingBox.contains(point)) {
            intersectRecursivePoint(point, tris);
        }
        return tris;
    }

    private void intersectRecursivePoint(Vector3f point, Set<Integer> tris) {
        if (intersectHit(tris)) {
            return;
        }
        for (Octree child : children) {
            if (child.boundingBox.contains(point)) {
                child.intersectRecursivePoint(point, tris);
            }
        }
    }

    /**
     * Intersect a BoundingBox with the Octree
     * @param bb the BoundingBox
     * @return List of triangle indexes to be used with TriangleBatch.getTriangle
     */
    public Set<Integer> intersectBox(BoundingBox bb) {
        onIntersectStart();
        Set<Integer> tris = new HashSet<Integer>();
        if (boundingBox.intersectsBoundingBox(bb)) {
            intersectRecursiveBox(bb, tris);
        }
        return tris;
    }

    private void intersectRecursiveBox(BoundingBox bb, Set<Integer> tris) {
        if (intersectHit(tris)) {
            return;
        }
        for (Octree child : children) {
            if (child.boundingBox.intersectsBoundingBox(bb)) {
                child.intersectRecursiveBox(bb, tris);
            }
        }
    }

    /**
     * Intersect a BoundingSphere with the Octree
     * @param bb the BoundingSphere
     * @return List of triangle indexes to be used with TriangleBatch.getTriangle
     */
    public Set<Integer> intersectSphere(BoundingSphere bs) {
        onIntersectStart();
        Set<Integer> tris = new HashSet<Integer>();
        if (boundingBox.intersectsSphere(bs)) {
            intersectRecursiveSphere(bs, tris);
        }
        return tris;
    }

    private void intersectRecursiveSphere(BoundingSphere bs, Set<Integer> tris) {
        if (intersectHit(tris)) {
            return;
        }
        for (Octree child : children) {
            if (child.boundingBox.intersectsSphere(bs)) {
                child.intersectRecursiveSphere(bs, tris);
            }
        }
    }

//    /**
//     * Intersect a BoundingCapsule with the Octree
//     * @param bb the BoundingCapsule
//     * @return List of triangle indexes to be used with TriangleBatch.getTriangle
//     */
//    public Set<Integer> intersectCapsule(BoundingCapsule bc) {
//        onIntersectStart();
//        Set<Integer> tris = new HashSet<Integer>();
//        if (boundingBox.intersectsCapsule(bc)) {
//            intersectRecursiveCapsule(bc, tris);
//        }
//        return tris;
//    }
//
//    private void intersectRecursiveCapsule(BoundingCapsule bc, Set<Integer> tris) {
//        if (intersectHit(tris)) {
//            return;
//        }
//        for (Octree child : children) {
//            if (child.boundingBox.intersectsCapsule(bc)) {
//                child.intersectRecursiveCapsule(bc, tris);
//            }
//        }
//    }

    /**
     * Intersect the camera's frustrum with the Octree
     * @param cam The camera
     * @return List of triangle indexes to be used with TriangleBatch.getTriangle
     */
    public Set<Integer> intersectCamera(Camera cam) {
        onIntersectStart();
        int state = cam.getPlaneState();
        Set<Integer> tris = new HashSet<Integer>();
        if (cam.contains(boundingBox) != FrustumIntersect.Outside) {
            intersectRecursiveCamera(cam, tris);
        }
        cam.setPlaneState(state);
        return tris;
    }

    private void intersectRecursiveCamera(Camera cam, Set<Integer> tris) {
        if (intersectHit(tris)) {
            return;
        }
        for (Octree child : children) {
            if (cam.contains(boundingBox) != FrustumIntersect.Outside) {
                child.intersectRecursiveCamera(cam, tris);
            }
        }
    }

    /* 
     * Methods to be overriten by extended implementations. Use only if needed. 
     */
    protected Octree newOctreeNode(BoundingBox bb, List<Integer> tris) {
        return new Octree(this, bb, tris);
    }

    protected void onSubdivide() {
    }

    protected void onRecurseFinished() {
    }

    protected void onIntersectStart() {
    }

    protected void onIntersectHit() {
    }
}
