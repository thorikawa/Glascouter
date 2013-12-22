#include "jni_scouter.hpp"
#include "Scouter.hpp"

using namespace std;

JNIEXPORT jlong JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeCreateObject
  (JNIEnv *jenv, jobject, jstring jFaceXml, jstring jNoseXml) {
	const char* faceXml = jenv->GetStringUTFChars(jFaceXml, NULL);
	const char* noseXml = jenv->GetStringUTFChars(jNoseXml, NULL);
	Scouter* scouter = new Scouter(string(faceXml), string(noseXml));
	return (jlong) scouter;
}

JNIEXPORT void JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeProcess
  (JNIEnv *jenv, jobject, jlong thiz, jlong image) {
	try {
		((Scouter*) thiz)->process(*((Mat*) image));
	} catch (...) {
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, "native error");
	}
}

JNIEXPORT void JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeDestroyObject
  (JNIEnv *, jobject, jlong) {

}

