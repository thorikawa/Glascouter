#include "jni_scouter.hpp"
#include "Scouter.hpp"

using namespace std;

inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat) {
	mat = Mat(v_rect, true);
}

JNIEXPORT jlong JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeCreateObject(
		JNIEnv *jenv, jobject, jstring jFaceXml, jstring jeyeXml1,
		jstring jeyeXml2, jstring jFaceModelFile) {
	const char* faceXml = jenv->GetStringUTFChars(jFaceXml, NULL);
	const char* eyeXml1 = jenv->GetStringUTFChars(jeyeXml1, NULL);
	const char* eyeXml2 = jenv->GetStringUTFChars(jeyeXml2, NULL);
	const char* faceModelFile = jenv->GetStringUTFChars(jFaceModelFile, NULL);
	Scouter* scouter = new Scouter(string(faceXml), string(eyeXml1),
			string(eyeXml2), string(faceModelFile));
	return (jlong) scouter;
}

JNIEXPORT jint JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeProcess(
		JNIEnv *jenv, jobject, jlong thiz, jlong image, jlong faceRectMat) {
	try {
		vector < Rect > faceRectVec;
		int power = ((Scouter*) thiz)->process(*((Mat*) image), faceRectVec);
		vector_Rect_to_Mat(faceRectVec, *((Mat*) faceRectMat));
		return (jint) power;
	} catch (...) {
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, "native error");
	}
}

JNIEXPORT void JNICALL Java_com_polysfactory_scouter_jni_ScouterProcessor_nativeDestroyObject
(JNIEnv *, jobject, jlong) {

}

