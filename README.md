# Targeted Projection Pursuit

<img src="https://user-images.githubusercontent.com/17095065/230709037-aaad44c2-efa0-4220-bc70-50f3b65f932c.gif" width=600>

TPP is a technique for visualising and exploring high-dimensional data for exploratory data analysis, visualisation and feature selection.

## Quick Start
- Check if you already have java installed with `java -version`. If not then install it from java.com.
- Download the executable jar file https://github.com/joefaith/targeted-projection-pursuit/blob/master/dist/TPP.jar
- You can also clone the repo. You will need Apache Ant to build it. VS Code has a very nice extension for Ant. https://marketplace.visualstudio.com/items?itemName=nickheap.vscode-ant
- Double click on the jar to run it, or run it from the command line with `java -jar TPP.jar`
- Load a data set from a CSV file (including a header row) with `File > Load CSV File`. 
- Drag and drop data points or axes to explore the data.

## Why Visualise Your Data?

Why bother visualising our data? After all, isn’t the whole point of machine learning to get machines to do the data analysis for us?

<img src="https://user-images.githubusercontent.com/17095065/230708116-28a063c3-f258-4b2e-a0b1-50c1e373e384.png" width="600">

This classic example from the statistics literature, known as Anscombe's Quartet, shows why visualisation is important. It shows four data sets with the same aggregate statistics: same means and standard deviations in each dimension, same linear regression slopes and residuals. But when you graph them you can immediately see they should be treated very differently. #2 should be modelled with a quadratic. #3 and #4 need some outliers detected and maybe removed.

Similar problems can occur with machine learning problems, such as classification. Suppose we had a classification problem and our model was consistently generalising at 90% accuracy. Is this good? What should we do? It depends on the data. 

<img src="https://user-images.githubusercontent.com/17095065/230708124-51763db1-8205-4249-9f53-b410cb6c5d5e.png" width="600">

Here are four binary classification problems (stars and circles), with the performance of four classifiers shown: the examples where the classifications are correctly inferred are solid, the incorrect examples are unfilled. We would do something different in each of these four cases.

- **Example A:** The classifier should be getting these right. Looks like a bug.
- **Example B:** Looks like the error cases were mis-labelled. Better check them.
- **Example C:** These classes aren’t clearly separable. 90% generalisation is probably the best we could get. Any further training could lead to overfitting.
- **Example D:** The error cases look like outliers. We should detect these and flag up to a human that our confidence in the classification is low in these cases.

## Why Targeted Projection Pursuit?

Visualising simple 2D cases is easy enough, but most of the data we deal with is far more complex – especially when dealing with latent representations. We need ways of visualising complex data in just two screen dimensions, and there are many ways to reduce the dimensionality of complex data so it can be visualised. The simplest is just to pick two or three of the dimensions and ignore the rest, but it's very hard to see what's going on.

<img src="https://user-images.githubusercontent.com/17095065/230708834-9479b5d3-7e5e-4978-ad55-bcb6f45d9d8e.png" width="600">

Other algorithms, such as projecting into principal components or t-SNE try to squeeze more dimensions into two or three, but this always results in the loss of potentially important information. No single view will show us everything we need. We need ways of 'rotating' the data, so we can see what it looks like from various angles. Visualisations that use linear projections – such as PCA – have an advantage in that the projection itself can provide useful information about the data, such as which dimensions are more important in classification. 

<img src="https://user-images.githubusercontent.com/17095065/230708185-662b706b-e001-4e73-b57c-241bd538815c.png" width="600">

Targeted Project Pursuit is the higher dimensional equivalent of rotating an object to explore it. The data is initially shown projected onto the first two principal components (X = PC1, Y = PC2), but you can then rotate the data to see it from other dimensions. You can  do this by dragging and dropping specific axes, or by grabbing and dragging the data itself. The TPP algorithm will then try to find and angle of the data that matches your actions most closely. The easiest way to see this is to try it yourself!

## Background
TPP was originally developed to visualise gene expression data, to help clinicians diagnose early-stage cancers. The code is mostly old (>10years) java, and built on top of the Weka machine learning package, and some features have succumbed to bit rot, but if it’s useful then I’ll progressively resurrect it. Let me know.
There's more information here, including links to relevant papers: https://en.wikipedia.org/wiki/Targeted_projection_pursuit

