#include "Scouter.hpp"
#include "SevenSegment.hpp"
#include "preprocessFace.h"

#define TRIANGLE_EDGE 40

const char *facerecAlgorithm = "FaceRecognizer.Fisherfaces";
const bool preprocessLeftAndRightSeparately = true;
const double powerVector[] = { 431439.8832739892, 194.6195068359375,
		7481.8276426792145, 4987.885095119476, 4914369.920417783,
		56815.128661595285, 2216.8378200531006, 1456109.6060497134,
		37876.75244106352, 38.443359375, 25.62890625, 17.0859375,
		656.8408355712891, 2184164.4090745705, 647159.8249109838, 5.0625,
		25251.16829404235, 11222.741464018822, 287626.58884932613, 1.5,
		57.6650390625, 16834.112196028233, 291.92926025390625,
		127834.03948858939, 1477.8918800354004, 191751.0592328841,
		3276246.6136118555, 129.746337890625, 7371554.880626675, 3.375, 1.0,
		970739.7373664756, 985.2612533569336, 86.49755859375, 2.25,
		3325.256730079651, 7.59375, 437.8938903808594, 11.390625,
		85222.69299239293 };
const double ratios[] = { 0.6, 0.3, 0.1 };

typedef std::pair<double, int> mypair;
bool comparator(const mypair& l, const mypair& r) {
	return l.first < r.first;
}

Scouter::Scouter(string faceCascadeFile, string eyeCascadeFile1,
		string eyeCascadeFile2, string faceModelFile) {

	faceCascade.load(faceCascadeFile);
	if (faceCascade.empty()) {
		LOGD("could not initialize face cascade");
	}
	eyeCascade1.load(eyeCascadeFile1);
	if (eyeCascade1.empty()) {
		LOGD("could not initialize eye cascade1");
	}
	eyeCascade2.load(eyeCascadeFile2);
	if (eyeCascade2.empty()) {
		LOGD("could not initialize eye cascade2");
	}

	model = Algorithm::create < FaceRecognizer > (facerecAlgorithm);
	model->load(faceModelFile);
	eigenvectors = model->get < Mat > ("eigenvectors");
	averageFaceRow = model->get < Mat > ("mean");

	// LOGD("load face model: %s", faceModelFile.c_str());
	LOGD("load eigen vectors: %d, %d", eigenvectors.rows, eigenvectors.cols);
	LOGD("average face row: %d, %d", averageFaceRow.rows, averageFaceRow.cols);
}

Scouter::~Scouter() {
}

int Scouter::process(Mat& rgba, vector<Rect>& faceRectVec) {

	int w = rgba.cols;
	int h = rgba.rows;
	/*
	Mat gray;
	cvtColor(rgba, gray, CV_RGBA2GRAY);

	Mat green(Size(w, h), CV_8UC4, Scalar(0, 255, 0, 255));
	Mat info(Size(w, h), CV_8UC4, Scalar(255, 255, 255, 0));
	*/

	// green overlay
	// addWeighted(rgba, 0.7, green, 0.3, 0.0, rgba);

	/*
	// detect faces
	vector < Rect > faces;
	faceCascade.detectMultiScale(gray, faces, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE,
			Size(50, 50), Size(400, 400));
	if (faces.size() == 0) {
		return -1;
	}

	// if find faces...
	// Rect rect = faces[0];
	// int minLength = min(rect.width, rect.height);
	Rect maxFace;
	int maxFaceEdge = -1;
	for (int i = 0; i < faces.size(); i++) {
		Rect rect = faces[i];
		faceRectVec.push_back(rect);
		Point center = Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
		// int radius = max(rect.width, rect.height);
		// int radius = (rect.width + rect.height) / 2;
		int radius = min(rect.width, rect.height) - 10;
		Point left = Point(center.x - radius - 10, center.y);
		Point bottom = Point(center.x, center.y + radius + 10);

		int faceEdge = max(rect.width, rect.height);
		if (maxFaceEdge < faceEdge) {
			maxFace = rect;
			maxFaceEdge = faceEdge;
		}
		// circle(rgba, center, radius, Scalar(200, 255, 200), 8);
		// rectangle(rgba, rect.tl(), rect.br(), Scalar(200, 255, 200), -1, 8);
		// LOGD("rect (%d,%d)", rect.x, rect.y);
		// LOGD("tl (%d,%d)", rect.tl().x, rect.tl().y);
	} */
	// Mat faceMat = rgba(maxFace);
	Rect faceRect; // Position of detected face.
	Rect searchedLeftEye, searchedRightEye; // top-left and top-right regions of the face, where eyes were searched.
	Point leftEye, rightEye; // Position of the detected eyes.
	Mat test = getPreprocessedFace(rgba, 92, 112, faceCascade, eyeCascade1,
			eyeCascade2, preprocessLeftAndRightSeparately, &faceRect, &leftEye,
			&rightEye, &searchedLeftEye, &searchedRightEye);
	LOGD("preprocessed (%d,%d)", test.cols, test.rows);
	if (test.cols <= 0 && test.rows <= 0) {
		return -1;
	}
	faceRectVec.push_back(faceRect);
	Mat projection = subspaceProject(eigenvectors, averageFaceRow,
			test.reshape(1, 1));
	vector<mypair> vec;
	for (int i = 0; i < projection.cols; i++) {
		double a = projection.at<double>(0, i);
		vec.push_back(mypair(a, i));
	}
	sort(vec.begin(), vec.end(), comparator);
	reverse(vec.begin(), vec.end());
	double power = 0;
	for (int i = 0; i < 3; i++) {
		power += ratios[i] * powerVector[vec[i].second];
	}
	return (int) power;
}
