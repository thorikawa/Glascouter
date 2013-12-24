#ifndef _SCOUTER_H_
#define _SCOUTER_H_

#include <opencv2/opencv.hpp>

#ifdef __ANDROID__
#include <android/log.h>
#define LOG_TAG "Scouter/Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);
#else
#define LOGD(...) printf(__VA_ARGS__);
#endif


using namespace std;
using namespace cv;

class Scouter {
private:
	CascadeClassifier* faceClassifier;
	CascadeClassifier* noseClassifier;
public:
	Scouter(string faceCascadeFile, string noseCascadeFile);
	~Scouter();
	int process(Mat &rgba, vector<Rect>& faceRectVec);
};

#endif
