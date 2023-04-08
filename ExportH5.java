// ImageJ macro to convert images from .tif to .h5
// Saves a copy of the .tif images in a new subfolder
// Generates .h5 images with an added particle in the top left corner and saves them in the sample folder

// Define input and output directories
main_dir = getDirectory("input");
internal_standard = main_dir + "Internal_Standard.tif";
open(internal_standard);

// Process each subdirectory in the main directory
directories = getFileList(main_dir);
for (i=0; i<directories.length; i++) {
	if (endsWith(directories[i], "/")) {
		directory = substring(directories[i], 0, directories[i].length-1);
		print("Processing Sample " + directory);
		export_h5(directory);
	}
}
close(internal_standard);

// Define a function to export .h5 file
function export_h5(dir) {
	// Get list of files in the subdirectory
	files = getFileList(main_dir + dir);
	if (files.length == 0) {
		print("Folder is empty");
		return;
	}

	// Create raw directory
	backslash = File.separator;
	working_dir = main_dir + dir + backslash;
	raw_dir = working_dir + "raw" + backslash;
	File.makeDirectory(raw_dir);
	files = getFileList(working_dir);
	// Process each file in the subdirectory
	open(internal_standard);
	for (i=0; i<files.length; i++) {
		if (endsWith(files[i], "/")) {
			continue;
		}
		print("File: " + files[i]);

		// Define variables for usage in opening and exporting
		inputPath = working_dir + files[i];
		name = files[i];
		prefix = name.substring(0, name.length()-3);
		suffix = "h5";
		select = working_dir + suffix;
		output_path = working_dir + prefix + suffix;

		// Open the .tif file and save the raw file in the raw folder
		open(inputPath);
		raw_file = raw_dir + name;
		saveAs("tif", raw_file);

		// Add the internal standard to the raw data
		imageCalculator("Add stack", name, "Internal_Standard.tif");

		// Run ilastik plugin export function
		exportArgs = "select=" + select + " exportpath=" + output_path + " datasetname=data compressionlevel=0 input=" + name;
		run("Export HDF5", exportArgs);

		// Delete the raw file in the original directory
		File.delete(inputPath);
	}
	print("Successfully processed sample " + dir);
}
