#include "Scouter.hpp"

Scouter::Scouter(string faceCascadeFile, string noseCascadeFile) {
	faceClassifier = new CascadeClassifier();
	faceClassifier->load(faceCascadeFile);

	noseClassifier = new CascadeClassifier();
	noseClassifier->load(noseCascadeFile);
}

Scouter::~Scouter() {
}

void Scouter::process(Mat& image) {

	int w = image.cols;
	int h = image.rows;
	// rectangle(image, Point(0.0, 0.0), Point(w, h), Scalar(0.0, 255.0, 0.0, 100.0), CV_FILLED);

	vector < Rect > faces;
	faceClassifier->detectMultiScale(image, faces, 1.1, 2, 0, Size(50, 50),
			Size(400, 400));
	if (faces.size() == 0) {
		return;
	}

	Rect rect = faces[0];
	rectangle(image, rect.tl(), rect.br(), Scalar(200, 200, 200));
	Mat faceMat = image(rect);
	vector < Rect > noses;
	noseClassifier->detectMultiScale(faceMat, noses, 1.1, 2, 0, Size(20, 20),
			Size(100, 100));
	if (noses.size() > 0) {
		int n = noses.size();
		for (int i = 0; i < n; i++) {
			Rect noseRect = noses[i];
			rectangle(faceMat, noseRect.tl(), noseRect.br(), Scalar(255, 0, 0));
		}
	}
}
