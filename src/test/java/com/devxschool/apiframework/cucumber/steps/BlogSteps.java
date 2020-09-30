package com.devxschool.apiframework.cucumber.steps;

import com.devxschool.apiframework.api.pojos.BlogPost;
import com.devxschool.apiframework.api.pojos.BlogUser;
import com.devxschool.apiframework.cucumber.steps.common.CommonData;
import com.devxschool.apiframework.utilities.ObjectConverter;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.util.List;
import java.util.Map;

public class BlogSteps {

    private CommonData commonData;
    private BlogPost blogPost;
    private String userId;

    public BlogSteps(CommonData commonData) {
        this.commonData = commonData;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://5f629b9c67e195001625f0a4.mockapi.io/api/v1";
    }

    @When("^the following user has been created:$")
    public void the_following_user_has_been_created(List<BlogUser> user) throws Throwable {
        RequestSpecification requestSpec = RestAssured.given();
        requestSpec.contentType(ContentType.JSON);
        requestSpec.body(user.get(0));

        commonData.response =  requestSpec.post("/users");

        userId = commonData.response.jsonPath().getString("id");
    }

    @When("^the following post has been created for the user$")
    public void the_following_post_has_been_created_for_the_user(List<BlogPost> post) throws Throwable {
        BlogUser blogUser = ObjectConverter.convertJsonObjectToJavaObject(commonData.response.getBody().asString(), BlogUser.class);

        RequestSpecification requestSpec = RestAssured.given();
        requestSpec.contentType(ContentType.JSON);
        requestSpec.body(post.get(0));

        commonData.response =  requestSpec
                .pathParam("userId", blogUser.getId())
                .post("/users/{userId}/posts");
    }

    @When("^the following comment has been left under a post$")
    public void the_following_comment_has_been_left_under_a_post(List<Map<String, String>> comment) throws Throwable {
        blogPost = ObjectConverter.convertJsonObjectToJavaObject(commonData.response.getBody().asString(), BlogPost.class);
        RequestSpecification requestSpec = RestAssured.given();
        requestSpec.contentType(ContentType.JSON);
        requestSpec.body(comment.get(0));

        commonData.response = requestSpec
                .pathParam("userId", blogPost.getUserId())
                .pathParam("postId", blogPost.getId())
                .post("/users/{userId}/posts/{postId}/comments");
    }

    @When("^the post details has been requested$")
    public void the_post_details_has_been_requested() throws Throwable {
        RequestSpecification requestSpec = RestAssured.given();
        requestSpec.contentType(ContentType.JSON);

        commonData.response = requestSpec
                .pathParam("userId", blogPost.getUserId())
                .pathParam("postId", blogPost.getId())
                .get("/users/{userId}/posts/{postId}");
    }

    @Then("^the following post has been returned$")
    public void the_following_post_has_been_returned(List<Map<String, String>> postDetails) throws Throwable {
        BlogPost actualPost = ObjectConverter.convertJsonObjectToJavaObject(commonData.response.getBody().asString(), BlogPost.class);

        MatcherAssert.assertThat(actualPost.getTitle(), Matchers.is(postDetails.get(0).get("title")));
        MatcherAssert.assertThat(actualPost.getDescription(), Matchers.is(postDetails.get(0).get("description")));
        MatcherAssert.assertThat(actualPost.getComments().get(0).getComment(), Matchers.is(postDetails.get(0).get("comment")));
        MatcherAssert.assertThat(actualPost.getUserId(), Matchers.is(userId));
    }
}
