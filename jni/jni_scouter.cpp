#include "jni_scouter.hpp"
#include "Scouter.hpp"

using namespace std;

inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat) {
	mat = Mat(v_rect, true);
}

JNIEXPORT jlong JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeCreateObject
  (JNIEnv *jenv, jobject, jstring jFaceXml, jstring jNoseXml) {
	const char* faceXml = jenv->GetStringUTFChars(jFaceXml, NULL);
	const char* noseXml = jenv->GetStringUTFChars(jNoseXml, NULL);
	Scouter* scouter = new Scouter(string(faceXml), string(noseXml));
	return (jlong) scouter;
}

JNIEXPORT void JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeProcess
  (JNIEnv *jenv, jobject, jlong thiz, jlong image, jlong faceRectMat) {
	try {
		vector<Rect> faceRectVec;
		((Scouter*) thiz)->process(*((Mat*) image), faceRectVec);
		vector_Rect_to_Mat(faceRectVec, *((Mat*)faceRectMat));
	} catch (...) {
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, "native error");
	}
}

JNIEXPORT void JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeDestroyObject
  (JNIEnv *, jobject, jlong) {

}

