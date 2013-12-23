#ifndef _SCOUTER_H_
#define _SCOUTER_H_

#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

class Scouter {
private:
	CascadeClassifier* faceClassifier;
	CascadeClassifier* noseClassifier;
public:
	Scouter(string faceCascadeFile, string noseCascadeFile);
	~Scouter();
	void process(Mat &image);
};

#endif
