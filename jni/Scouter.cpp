#include "Scouter.hpp"
#include "SevenSegment.hpp"

#define TRIANGLE_EDGE 40

Scouter::Scouter(string faceCascadeFile, string noseCascadeFile) {
	faceClassifier = new CascadeClassifier();
	faceClassifier->load(faceCascadeFile);

	noseClassifier = new CascadeClassifier();
	noseClassifier->load(noseCascadeFile);

	cv::Mat red_img(cv::Size(640, 480), CV_8UC3, cv::Scalar(0, 0, 255));
}

Scouter::~Scouter() {
}

void Scouter::process(Mat& rgba, vector<Rect>& faceRectVec) {

	int w = rgba.cols;
	int h = rgba.rows;
	Mat gray;
	cvtColor(rgba, gray, CV_RGBA2GRAY);

	Mat green(Size(w, h), CV_8UC4, Scalar(0, 255, 0, 255));
	Mat info(Size(w, h), CV_8UC4, Scalar(255, 255, 255, 0));

	// green overlay
	// addWeighted(rgba, 0.7, green, 0.3, 0.0, rgba);

	// detect faces
	vector < Rect > faces;
	faceClassifier->detectMultiScale(gray, faces, 1.1, 2,
			0 | CV_HAAR_SCALE_IMAGE, Size(50, 50), Size(400, 400));
	if (faces.size() == 0) {
		return;
	}

	// if find faces...
	// Rect rect = faces[0];
	// int minLength = min(rect.width, rect.height);
	for (int i = 0; i < faces.size(); i++) {
		/*
		 Rect tmpRect = faces[i];
		 int tmpMinLength = min(rect.width, rect.height);
		 if (tmpMinLength > minLength) {
		 rect = tmpRect;
		 minLength = tmpMinLength;
		 }
		 */
		Rect rect = faces[i];
		faceRectVec.push_back(rect);
		Point center = Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
		// int radius = max(rect.width, rect.height);
		// int radius = (rect.width + rect.height) / 2;
		int radius = min(rect.width, rect.height) - 10;
		Point left = Point(center.x - radius - 10, center.y);
		Point bottom = Point(center.x, center.y + radius + 10);

		// circle(rgba, center, radius, Scalar(200, 255, 200), 8);
		// rectangle(rgba, rect.tl(), rect.br(), Scalar(200, 255, 200), -1, 8);
		LOGD("rect (%d,%d)", rect.x, rect.y);
		LOGD("tl (%d,%d)", rect.tl().x, rect.tl().y);
	}

	return;
}
