package uk.ac.wlv.blogger;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BlogDetailActivity extends AppCompatActivity {

    private EditText editBlogName, editBlogBody;
    private ImageView blogImage;
    private Button saveBtn, deleteBtn, goBackBtn, shareBtn, emailBtn;
    private SQLiteDatabase db;
    private int blogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

        // Initialize UI components
        editBlogName = findViewById(R.id.editBlogName);
        editBlogBody = findViewById(R.id.editBlogBody);
        blogImage = findViewById(R.id.blogImage);
        saveBtn = findViewById(R.id.saveBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        goBackBtn = findViewById(R.id.goBackBtn);
        shareBtn = findViewById(R.id.shareBtn);
        emailBtn = findViewById(R.id.emailBtn);

        // Create or open the SQLite database
        db = openOrCreateDatabase("blogsdb", MODE_PRIVATE, null);

        // Retrieve blog details from the intent
        blogId = getIntent().getIntExtra("blogId", -1);
        String blogName = getIntent().getStringExtra("blogName");
        String blogBody = getIntent().getStringExtra("blogBody");
        String imageUriString = getIntent().getStringExtra("imageUri");

        Log.d("BlogDetailActivity", "Received Image URI: " + imageUriString);
        // Set retrieved blog details to the UI components
        editBlogName.setText(blogName);
        editBlogBody.setText(blogBody);

        // Load and display image
        if (imageUriString != null) {

            Uri imageUri = Uri.parse(imageUriString);
            loadImage(imageUri);
        }

        // Set click listener for the 'Save' button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBlogChanges();
            }
        });

        // Set click listener for the 'Delete' button
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBlog();
            }
        });

        // Set click listener for the 'Go Back' button
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the result to OK and finish the activity
                setResult(RESULT_OK);
                finish(); // Close the BlogDetailActivity and go back to the MainActivity
            }
        });

        // Set click listener for the 'Share Blog' button
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBlog();
            }
        });

        // Set click listener for the 'Share Blog via email' button
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBlogViaEmail();
            }
        });
    }

    private void loadImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            blogImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveBlogChanges() {
        String updatedName = editBlogName.getText().toString().trim();
        String updatedBody = editBlogBody.getText().toString().trim();

        if (updatedName.isEmpty() || updatedBody.isEmpty()) {
            Toast.makeText(this, "Please enter both name and body", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update data in the 'blogtable'
        ContentValues values = new ContentValues();
        values.put("name", updatedName);
        values.put("body", updatedBody);

        int result = db.update("blogtable", values, "id=?", new String[]{String.valueOf(blogId)});

        if (result > 0) {
            Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error updating blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBlog() {
        int result = db.delete("blogtable", "id=?", new String[]{String.valueOf(blogId)});

        if (result > 0) {
            Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();
            // Set the result to OK and finish the activity
            setResult(RESULT_OK);
            finish(); // Close the BlogDetailActivity after deletion
        } else {
            Toast.makeText(this, "Error deleting blog", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareBlog() {
        String blogName = editBlogName.getText().toString().trim();
        String blogBody = editBlogBody.getText().toString().trim();

        if (blogName.isEmpty() || blogBody.isEmpty()) {
            Toast.makeText(this, "Blog details are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a share intent
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Blog Name: " + blogName + "\n\n" + "Blog Body: " + blogBody);
        shareIntent.setType("text/plain");

        // Show the share dialog
        startActivity(Intent.createChooser(shareIntent, "Share Blog using"));
    }

    private void shareBlogViaEmail() {
        String blogName = editBlogName.getText().toString().trim();
        String blogBody = editBlogBody.getText().toString().trim();
        if (blogName.isEmpty() || blogBody.isEmpty()) {
            Toast.makeText(this, "Blog details are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an email intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, blogName); // Set the subject to the blog name
        emailIntent.putExtra(Intent.EXTRA_TEXT, blogBody);

        // Show the email client chooser
        startActivity(Intent.createChooser(emailIntent, "Send Blog via Email"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}