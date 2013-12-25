#ifndef _SCOUTER_H_
#define _SCOUTER_H_

#include <opencv2/opencv.hpp>
#include "common.hpp"

using namespace std;
using namespace cv;

class Scouter {
private:
	CascadeClassifier faceCascade;
	CascadeClassifier noseCascade;
	CascadeClassifier eyeCascade1;
	CascadeClassifier eyeCascade2;
	Ptr<FaceRecognizer> model;
	Mat eigenvectors;
	Mat averageFaceRow;
public:
	Scouter(string faceCascadeFile, string eyeCascadeFile1,
			string eyeCascadeFile2, string faceModelFile);
	~Scouter();
	int process(Mat &rgba, vector<Rect>& faceRectVec);
};

#endif
