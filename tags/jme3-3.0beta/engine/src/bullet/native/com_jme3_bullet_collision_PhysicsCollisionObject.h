/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_jme3_bullet_collision_PhysicsCollisionObject */

#ifndef _Included_com_jme3_bullet_collision_PhysicsCollisionObject
#define _Included_com_jme3_bullet_collision_PhysicsCollisionObject
#ifdef __cplusplus
extern "C" {
#endif
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_NONE
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_NONE 0L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_01
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_01 1L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_02
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_02 2L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_03
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_03 4L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_04
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_04 8L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_05
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_05 16L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_06
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_06 32L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_07
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_07 64L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_08
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_08 128L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_09
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_09 256L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_10
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_10 512L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_11
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_11 1024L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_12
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_12 2048L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_13
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_13 4096L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_14
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_14 8192L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_15
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_15 16384L
#undef com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_16
#define com_jme3_bullet_collision_PhysicsCollisionObject_COLLISION_GROUP_16 32768L
/*
 * Class:     com_jme3_bullet_collision_PhysicsCollisionObject
 * Method:    initUserPointer
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_com_jme3_bullet_collision_PhysicsCollisionObject_initUserPointer
  (JNIEnv *, jobject, jlong, jint, jint);

/*
 * Class:     com_jme3_bullet_collision_PhysicsCollisionObject
 * Method:    attachCollisionShape
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_jme3_bullet_collision_PhysicsCollisionObject_attachCollisionShape
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_jme3_bullet_collision_PhysicsCollisionObject
 * Method:    setCollisionGroup
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_jme3_bullet_collision_PhysicsCollisionObject_setCollisionGroup
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_jme3_bullet_collision_PhysicsCollisionObject
 * Method:    setCollideWithGroups
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_jme3_bullet_collision_PhysicsCollisionObject_setCollideWithGroups
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_jme3_bullet_collision_PhysicsCollisionObject
 * Method:    finalizeNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_jme3_bullet_collision_PhysicsCollisionObject_finalizeNative
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif