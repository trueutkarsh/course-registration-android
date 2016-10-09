package com.example.trueutkarsh.courseregistration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trueutkarsh.courseregistration.dbwrappers.Course;
import com.example.trueutkarsh.courseregistration.util.BackgroundTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class RegistrationActivity extends AppCompatActivity {


    private LoadCourses loadCourses = null;
    private DeleteCourse delCourse = null;
    private ListView regCourselist = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getApplicationContext(), AddCoursesActivity.class);
                startActivity(intent);
            }
        });


        //Set load courses to get new values
        attemptGetCourses();


    }

    private void attemptGetCourses() {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", LoginActivity.ID);
        loadCourses = new LoadCourses("/register", "POST", params);
        loadCourses.execute((Void) null);

    }

    private void attemptDeleteCourse(Course course) {

        Boolean result;
        HashMap<String, String> params = new HashMap<>();
        params.put("id", LoginActivity.ID);
//        params.put("requestType", "delete");
//        params.put("course_id", course.getCourse_id());
//        params.put("sec_id", course.getSec_id());
        params.put("data", "delete," + course.getCourse_id() + "," + course.getSec_id());
        delCourse = new DeleteCourse("/AddDelete", "POST", params);

        try {
            JSONObject response = new JSONObject(delCourse.execute((Void) null).get());

            result = checkIfDeleted(response);

            if(result){
                regCourselist.setAdapter(null);// remove previous content
                showCourses(response); // add new list of courses
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private Boolean checkIfDeleted(JSONObject response) {
        try {
            if(response.getString("status").equals("true")){
                return true;
            }

            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Boolean showCourses(JSONObject response) {
        //load courses here
        Boolean result = true;
        List<Course> courses = new ArrayList<>();
        try {
            if(response.getString("status").equals("true")) {
                JSONArray jCourses = response.getJSONArray("data");
                JSONObject temp;
                Course c;
                courses.clear();
                for (int i = 0; i < jCourses.length(); i++) {
                    temp = jCourses.getJSONObject(i);
                    c = new Course(temp.getString("course_id"), temp.getString("sec_id"), temp.getString("title"), temp.getString("dept_name"), temp.getString("credits"));
                    courses.add(c);
                }
                regCourselist = (ListView) findViewById(R.id.regcourses_list);
                regCourselist.setAdapter(null);

                if(!courses.isEmpty()){
                    mListAdapter listAdapter = new mListAdapter(this, courses);
                    regCourselist.setAdapter(listAdapter);
                    showSnackbar("Here are your registered courses");
                }
                else{
                    showSnackbar("You havent taken any courses. Please add");
                }


            }
            else{
                showToast(response.getString("message"));
                result = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            result = false;
            showToast("Invalid JSON in request. Quitting.");
        }
        return result;

    }


    private class LoadCourses extends BackgroundTask {

        ProgressDialog progDialog;
        public LoadCourses(String url, String type, HashMap<String, String> entities) {
            super(url, type, entities);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog= new ProgressDialog(RegistrationActivity.this);
            progDialog.setMessage("Loading Your Courses...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected void onPostExecute(String success) {
            progDialog.dismiss();
            super.onPostExecute(success);

            if(this.result) {
                showCourses(response);

            }
            else{
                //TODO make a blank screen if necessary with no results
                //Toast.makeText(RegistrationActivity.this, "Sorry Could'nt load courses. Finishing", Toast.LENGTH_LONG).show();
                showToast("Sorry Could'nt load courses. Finishing");
                finish();
            }
        }
    };


    private class DeleteCourse extends LoadCourses{


        public DeleteCourse(String url, String type, HashMap<String, String> entities) {
            super(url, type, entities);
        }

    }


    private class mListAdapter extends BaseAdapter {

        List<Course> courses;
        Activity activity;
        private TextView cid_tv;
        private TextView sid_tv;
        private TextView title_tv;
        private TextView dept_tv;
        private TextView cred_tv;
        private Button remove_but;

        public mListAdapter(Activity activity, List<Course> courses){
            this.courses = courses;
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return courses.size();
        }

        @Override
        public Object getItem(int position) {
            return courses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

//            convertView = inflate(context, R.layout.listview_item_row, parent);

            LayoutInflater inflater=activity.getLayoutInflater();

            
            if(convertView == null){

                convertView=inflater.inflate(R.layout.listview_item_row, parent, false);

                cid_tv=(TextView) convertView.findViewById(R.id.course_id);
                sid_tv=(TextView) convertView.findViewById(R.id.sec_id);
                title_tv=(TextView) convertView.findViewById(R.id.title);
                dept_tv=(TextView) convertView.findViewById(R.id.dept_name);
                cred_tv=(TextView) convertView.findViewById(R.id.credits);
                remove_but = (Button)convertView.findViewById(R.id.remove);

                remove_but.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO make another sync call attempt delete course

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                            builder.setTitle("Confirmation popup");
                            builder.setMessage("Do you really want to delete the course?");
                            builder.setNegativeButton("NO",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            showSnackbar("Course not deleted");
                                        }
                                    });
                            builder.setPositiveButton("YES",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {

                                            //deleteTask.execute((Void) null);
                                            attemptDeleteCourse(courses.get(position));
                                            showSnackbar("Course deleted Successfully");

                                        }
                                    });
                            builder.show();

                    }
                });

            }

            Course obj=courses.get(position);

            cid_tv.setText(obj.getCourse_id());
            sid_tv.setText(obj.getSec_id());
            title_tv.setText(obj.getTitle());
            dept_tv.setText(obj.getDept_name());
            cred_tv.setText(obj.getCredits());

            return convertView;
        }
    }



    private void showToast(String msg){

        Toast.makeText(RegistrationActivity.this, msg, Toast.LENGTH_LONG).show();

    }

    private void showSnackbar(String msg){
        Snackbar.make(findViewById(R.id.regcourses_list),msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        attemptGetCourses();
    }


}
