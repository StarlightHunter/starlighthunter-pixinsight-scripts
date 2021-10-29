/*
-------------------------------------------------------------------------------
StarlightHunter.com Script utilities
-------------------------------------------------------------------------------

MIT License

Copyright (c) 2021 Oliver Gutiérrez - StarlightHunter.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-------------------------------------------------------------------------------
*/


/*
 * Constants
 */


var CHANNEL_NAMES = {
   0: "R",
   1: "G",
   2: "B"
}


/*
 * Console helpers
 */


function print_header(text) {
   Console.writeln("");
   Console.writeln("-------------------------------------------------------------------------------");
   Console.writeln(text);
   Console.writeln("-------------------------------------------------------------------------------");
   Console.writeln("");
   Console.flush();
}


function print_message(text) {
   Console.write(text, "\n\n");
   Console.flush();
}


function print_submessage(text) {
   Console.write("  * ", text, "\n");
   Console.flush();
}


function print_footer(text) {
   print_header(text);
}


function do_scnr(view) {
   var P = new SCNR();
   P.executeOn(view);
   return view;
}


/*
 * Window management
 */


function create_window_for_image(image, id, show) {
   var newWin = new ImageWindow(image.width, image.height, image.numberOfChannels);

   if (id) newWin.mainView.id = id;
   if (show) newWin.show();

   image.firstSelectedChannel = 0;
   image.lastSelectedChannel = image.numberOfChannels - 1;

   newWin.mainView.beginProcess();
   newWin.mainView.image.assign(image);
   newWin.mainView.endProcess();
   return newWin.mainView;
}


/*
 * Channel extraction
 */


function extract_channel(image, channel) {
   var newImg = new Image();
   image.selectedChannel = channel;
   newImg.assign(image);
   return newImg;
}


function extract_rgb_channels(view, show) {
   show = show || false
   var views = [];
   for (var channel = 0; channel < 3; channel++) {
      var img = extract_channel(view.image, channel);
      var newView = create_window_for_image(img, CHANNEL_NAMES[channel], show);
      views.push(newView);
   }
   return views;
}


/*
 * PixInsight process helpers
 */


function do_pixel_math(view, expression) {
   var P = new PixelMath();
   P.expression = expression
   P.executeOn(view);
   return view;
}


function do_multiexpression_pixel_math(view, r_expr, g_expr, b_expr, a_expr) {
   var P = new PixelMath();
   P.useSingleExpression = false;
   P.expression0 = r_expr || "";
   P.expression1 = g_expr || "";
   P.expression2 = b_expr || "";
   P.expression3 = a_expr || "";
   P.executeOn(view);
   return view;
}


function do_curves_transform(view, amounts) {
   var P = new CurvesTransformation();

   for(elem in amounts) {
      P[elem] = [ // x, y
         [0.00000, 0.00000],
         [(1 - amounts[elem]), 1.00000],
         [1.00000, 1.00000]
      ];
      P[elem + "t"] = CurvesTransformation.prototype.Linear;
   }

   // Perform the transformation
   P.executeOn(view);
}


function do_linear_fit(view, reference) {
   var P = new LinearFit();
   P.referenceViewId = reference.id;
   P.executeOn(view);
   return view;
}
