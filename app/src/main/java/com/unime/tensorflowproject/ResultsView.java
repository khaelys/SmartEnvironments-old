package com.unime.tensorflowproject;

import com.unime.tensorflowproject.Classifier.Recognition;

import java.util.List;

public interface ResultsView {
    public void setResults(final List<Recognition> results);
}
