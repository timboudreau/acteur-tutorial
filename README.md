Acteur Tutorial
===============

This is a simple tutorial for [a simple framework](https://github.com/timboudreau/acteur) for writing simple, yet fast and scalable servers.

To set expectations:  If you are looking for something to run servlets, this is not it.  Acteur is great for creating very lightweight, very scalable servers.  Such servers can be quite powerful.  In some ways the programming model is lower-level than servlets - one of the goals was to make it easy to do things like HTTP cache headers *really* right.  Its target is to provide a similar in flavor programming model to what you get with Node.js - which is also low-level.  It is easy to quickly build up fairly high-level reusable components on top of the framework.


### Getting Started

To start out with, we need to create a Maven project and add a dependency:
```xml
	<dependency>
	    <version>1.5.4</version>
	    <type>jar</type>
	    <groupId>com.mastfrog</groupId>
	    <artifactId>acteur</artifactId>
	</dependency>
```
You might want to check what the most recent version of acteur is and set the ``<version>`` tag to that.

You'll want to add a "repositories" section to your ``pom.xml`` as well, [as described here](http://timboudreau.com/builds).

Creating the Application
------------------------

To start out with, we need a main class - acteur servers are *simple Java applications* which you run by running them.  It will consist of a subclass of ``Application`` and a ``main()`` method:

```java
import com.mastfrog.acteur.server.ServerBuilder;
import com.mastfrog.acteur.util.ServerControl;
import java.io.IOException;

public class TodoListApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerControl control = new ServerBuilder().build().start(port);
        control.await();
    }
}
```
This doesn't do much that is exciting - it just takes an optional command-line argument for what port to run on, starts a server on port 8134 or whatever it was passed, and waits for it to exit.  ``ServerBuilder`` hides the complexity of setting up Guice modules and building an injector.
``ServerControl`` allows you to cleanly shut down a server you have started.


### Writing an Acteur

SignerUpper is our first Acteur, which we'll add next to TodoListApp.  This Acteur is an *HTTP endpoint*, meaning it specifies a URL path
or other information that the framework will use to decide if it should handle the incoming request.  The `@HttpCall` annotation marks
it as such.  Additional annotations place constraints on when it will be called, versus either rejecting the request or moving on
to try another endpoint.

```java
@HttpCall
@PathRegex(SIGN_UP_PATTERN)
@RequiredUrlParameters("displayName")
@Methods(PUT)
@InjectRequestBodyAs(String.class)
final class SignerUpper extends Acteur {

    static final String SIGN_UP_PATTERN = "^users/(.*?)/signup$";

    @Inject
    SignerUpper(String password, Path path) throws IOException {
        if (password.length() < 8) {
            setState(new RespondWith(HttpResponseStatus.BAD_REQUEST, "Password must be at least 8 characters"));
            return;
        }
        String userName = path.getElement(1).toString();
        ok("Congratulations, " + userName + ", your password is " + password + "\n");
    }
}

In this case ask for the request body to be injected into our `SignerUpper` as a string.  We could also define a custom
data type that is deserializable from JSON with Jackson and use that instead.

Acteurs can be endpoints, or they can just contribute some portion of processing the request - in fact, each annotation
on this class actually specifies an Acteur inside the framework which will be called (and can potentially reject the request)
before ours gets called.  That means you can cleanly separate *validation logic* that determines if the input is usable
from *business logic* that does something with the input.

You can supply your own annotations and a `PageAnnotationHandler` to convert them to Acteurs for this sort of validation;
the framework supports a lot out of the box.

The call to `ok()` sets the *state* of the acteur.  Every Acteur returns a state.  If constructors could have a return
type, the state would be the return value - that's the best way to think of it.  That line could also have been written as

```java
setState(new RespondWith(HttpResponseStatus.OK, "Congratulations, " + userName + ", your password is " + password + "\n"));
```

[RespondWith](http://timboudreau.com/builds/job/acteur/lastSuccessfulBuild/artifact/acteur/target/site/apidocs/com/mastfrog/acteur/Acteur.RespondWith.html) is a subclass of [State](http://timboudreau.com/builds/job/acteur/lastSuccessfulBuild/artifact/acteur/target/site/apidocs/com/mastfrog/acteur/State.html).  An Acteur must either call ``setState()`` in its constructor, or override ``getState()``.  There are four states you will typically use, which are inner classes of ``Acteur`` and cannot be instantiated except there:

  * ``RespondWith`` - finish the request, using a specific response code and optional String or Object message.  Object messages are converted to JSON.
  * ``ConsumedLockedState`` - The current page will consume the current request;  optionally you can pass an array of objects for injection into subsequent acteurs.  For example, if you've authenticated a User, you might add a User object for later Acteurs to get in their constructor arguments and use
  * ``ConsumedState`` - The request is not rejected, and the next Acteur in the chain should be given a shot at it
  * ``RejectedState`` - This ``Page`` cannot respond to this request, but another may be able to - don't abort processing the request, but don't try any more Acteurs for the current page.

#### Pages

Earlier versions of this tutorial had you subclass `Page`.  The `@HttpCall` annotation causes this class to be generated for you.  You
may still use it in a few specific cases, but usually it is less coding to write Acteurs with annnotations and let the framework
do the work.


## Running the Application

At this point, we have something we can try out.  So build and run the maven project.  In a second or so, you will see something like

    Starting com.mastfrog.acteur.server.ServerImpl@6f2735cc on port 8134

printed on the command-line and it's ready to go.  We'll test it using the command-line utility ``curl``, which is available on almost any system:

	curl -i --data "acteursAreCool" -XPUT http://localhost:8134/users/tim/signup?displayName=Tim+Boudreau
	HTTP/1.1 200 OK
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.SignerUpper
	X-Page: com.mastfrog.acteur.tutorial.v1.SignUpPage
	Content-Length: 54
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 02:35:25 GMT
	X-Req-Path: users/tim/signup

	Congratulations, tim, your password is acteursAreCool

It's important also to verify that bad input works as expected:

	curl -i --data "short" -XPUT http://localhost:8134/users/tim/signup?displayName=Tim+Boudreau
	HTTP/1.1 400 Bad Request
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.SignerUpper
	X-Page: com.mastfrog.acteur.tutorial.v1.SignUpPage
	Content-Length: 38
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 02:40:52 GMT
	X-Req-Path: users/tim/signup

	Password must be at least 8 characters

and we should also verify that it won't work without a display name parameter:

	curl -i --data "password" -XPUT http://localhost:8134/users/tim/signup
	HTTP/1.1 400 Bad Request
	Allow: PUT
	Content-Type: text/plain; charset=utf-8
	X-Acteur: com.mastfrog.acteur.ActeurFactory$1RequireParameters
	X-Page: com.mastfrog.acteur.tutorial.v1.SignUpPage
	Content-Length: 36
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 02:41:57 GMT
	X-Req-Path: users/tim/signup

	Missing URL parameter 'displayName'

We can also test out the help page by going to [localhost:8134/help?html=true](view-source:http://localhost:8134/help?html=true) in a browser.  That's enabled by calling `enableHelp()` on our `ServerBuilder` - it uses the Page and Acteur classes, and their annotations to generate a generic HTML
help page, be default findable at the URL `/help` - this makes web APIs developed with Acteur self-documenting.  Add the `@Description` page to your
acteurs to give them friendly descriptions.

The project as described up to this point can be found in the ``acteur-tutorial-v1`` project on GitHub.

Wiring up a Database
====================

If we want users to sign up, we need to store that information somewhere.  This tutorial will use [MongoDB](http://mongodb.org) for that, and use Guice to inject the objects we need so that our Acteurs and Pages can simply ask for a ``Db`` or a ``DbCollection`` by putting it in their list of constructor arguments.

The first thing we'll do is add a dependency on MongoDB.  For the case of MongoDB, there is a small project which is part of Acteur which offers simple bindings for MongoDB.  We'll use that, but to be clear, you could easily wire it up however you like - and might want to for dealing with things like clustering and sharding.  For now, just add this dependency to the
Maven project's ``pom.xml``:
```xml
        <dependency>
            <groupId>com.mastfrog</groupId>
            <artifactId>acteur-mongo</artifactId>
            <version>${mastfrog.version}</version>
        </dependency>
```
If you don't have MongoDB installed somewhere, install it locally (or set up an account with a vendor which offers MongoDB - there are a few).  The following script is handy on Unix-like OS's to start MongoDB for testing applications, with some command-line switches to keep it from allocating giant files:
```sh
	#!/bin/sh
	if [ ! -d /tmp/mongodb ] ; then
		mkdir -p /tmp/mongodb
	fi
	mongod --dbpath /tmp/mongodb --nojournal --smallfiles --nssize 1 --noprealloc --slowms 5
```
Go ahead and start MongoDB now, and the rest of the tutorial will assume it is running.

<h1>************ UNEDITED CONTENT FOLLOWS **********</h1>

The second thing we'll do is change the way we're initializing our application a little bit, in ``TodoListApp.main()`` - after assigning the ``module`` variable, add this:
```java
        module.add(new MongoModule("todo")
                .bindCollection("users", "todoUsers")
                .bindCollection("todo"));
```
This will allow us to, very simply, use Guice's ``@Named`` to inject named collections;  we're passing the database name to MongoModule's constructor, and setting up bindings for two collections (collections are like SQL tables).  Here we have bound a collection in the database named ``todoUsers`` so that it can be injected like this:
```java
    @Inject
    public SomeConstructor ( @Named("users") DBCollection collection) { ... }
```
In the case of the "todo" collection, we will use the same collection name in code and in the database, so we just pass ``"todo"`` to ``bindCollection()`` to use the same name for both.

Since we're using injection, our code is not at all tied to the details of how the database is located - if we wanted to give every user their own shard, we could do that and not need to modify the rest of our code.


### Implementing New User Signup

Now we are ready to use the database, so that when someone signs up, the information they send is actually stored somewhere.  First, we'll change the signature of the constructor
```java
    @Inject
    SignerUpper(Event evt, @Named("users") DBCollection users, PasswordHasher hasher) throws IOException {
```
Next we will replace everything *after* the line 
```java
        String userName = evt.getPath().getElement(1).toString();
```
with the following code.  First, we create a query object to check if a user with this name already exists:
```java
        BasicDBObject query = new BasicDBObject("name", userName);
```
Next we perform the query:
```java
        DBObject result = users.findOne(query);
```
If we get non-null as a result, then there is already a user, so we'll return an HTTP 409 CONFLICT response:
```java
        if (result != null) {
            setState(new RespondWith(HttpResponseStatus.CONFLICT, "A user named " 
                    + userName + " exists\n"));
        } else {
```
If there is no such user, then we can reuse our query object to write into the database.  First we'll add the password - not the literal password, since storing passwords in the clear is insecure, but a hashed version of it.  By default ``hasher.encryptPassword`` uses SHA-512 to hash the password.  When authenticating, if the user's password, once hashed, matches what we have stored, then it is the same password:
```java
            query.put("password", hasher.encryptPassword(password));
```
Next we'll add the display name:
```java
            query.put("displayName", displayName);
```
And write it to the database:
```java
            users.save(query);
```
After the write, we will have an ID for the newly created user record, stored in the ``_id`` field by MongoDB.  Let's pass that back in the response:
```java
            setState(new RespondWith(HttpResponseStatus.OK, "Congratulations, "
                    + userName + ", you are  " + query.get("_id") + "\n"));
        }
    }
```
Once again, we can test the code to make sure it works:
```sh
	curl -i --data "acteursAreCool" -XPUT http://localhost:8134/users/tim/signup?displayName=Tim+Boudreau
	HTTP/1.1 200 OK
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.SignerUpper
	X-Page: com.mastfrog.acteur.tutorial.v1.SignUpPage
	Content-Length: 55
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 05:07:03 GMT
	X-Req-Path: users/tim/signup

	Congratulations, tim, you are  5162507727364e4c50c69d7c
```
and if we try it again, we should get a 409 CONFLICT response, and indeed we do:
```sh
	curl -i --data "acteursAreCool" -XPUT http://localhost:8134/users/tim/signup?displayName=Tim+Boudreau
	HTTP/1.1 409 Conflict
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.SignerUpper
	X-Page: com.mastfrog.acteur.tutorial.v1.SignUpPage
	Content-Length: 23
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 05:07:13 GMT
	X-Req-Path: users/tim/signup

	A user named tim exists
```
The project as described up to this point can be found in the ``acteur-tutorial-v2`` project on GitHub.


Adding Authentication and Using The Database
============================================

Next we will want to add HTTP basic authentication and a very simple "who am I" page which allows a client to test if it is authenticated and get back some JSON information about the logged in user.

First we'll create a very simple User object - part of the design philosophy of Acteur is not to impose a lot of concepts such as canned "user" types on you, but make it trivially simple to implement and use your own using plain Java objects.  Here's the POJO:
```java
	final class User {
	    public final String id;
	    public final String name;
	    public final String displayName;

	    @JsonCreator
	    User(String id, String name, String displayName) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
	    }
	}
```
The User type will be injected into Acteurs - that way, we deal with authentication once and reuse it.  If we want we could have it implement, say, [java.security.Principal](http://docs.oracle.com/javase/7/docs/api/java/security/Principal.html), but given that it's not a class that actually does anything useful, we don't have to use it unless we want to interoperate with something that expects it.

Guice needs to be explicitly told what types are going to be bound, and only those types are available for injection.  Acteur lets you annotate your ``Application`` subclass with an annotation ``ImplicitBindings`` with an array of classes which will be dynamically generated.  So we add that annotation to ``TodoListApp``:
```java
    @ImplicitBindings(User.class)
    public class TodoListApp extends Application {
```

### Implementing Authentication

Acteur has built-in support for basic authentication, which we will reuse (although it would be trivial to write it from scratch).  To do that we need to implement an interface called Authenticator.  It has one method:
```java
    public Object[] authenticate(String realm, BasicCredentials credentials) throws IOException;
```
If it returns null, then authentication failed.  If it returns non-null, then the objects it outputs will be available for injection into the next ``Acteur`` in the chain.

We will create a class named ``AuthenticatorImpl`` which implements it.  First, though, lets set up our Guice binding for it. For this we need our own Guice module. Add this in the main method:
```java
        module.add(new TodoListModule());
```
and add this toward the bottom of ``TodoListApp``:
```java
    static class TodoListModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Authenticator.class).to(AuthenticatorImpl.class);
        }
    }
```
Now we will implement ``Authenticator``.  It will do much the same user lookup code as we did earlier, but it will check the password against the one stored in the database.  If the password matches, then a User object will be available for injection into subsequent ``Acteurs``.  So those Acteurs don't have to worry about authentication directly at all - they just request a ``User`` object as one of their parameters:
```java
    final class AuthenticatorImpl implements Authenticator {
        private final DBCollection users;
        private final PasswordHasher hasher;

        @Inject
        AuthenticatorImpl(@Named(value = "users") DBCollection users, PasswordHasher hasher) {
            this.users = users;
            this.hasher = hasher;
        }

        @Override
        public Object[] authenticate(String realm, BasicCredentials credentials) throws IOException {
            BasicDBObject query = new BasicDBObject("name", credentials.username);
            DBObject userRecord = users.findOne(query);
            if (userRecord != null) {
                String password = (String) userRecord.get("password");
                if (hasher.checkPassword(credentials.password, password)) {
                    User user = new User(userRecord.get("_id") + "", 
                            (String) userRecord.get("name"), 
                            (String) userRecord.get("displayName"));
                    return new Object[]{user};
                }
            }
            return null;
        }
    }
```

Adding a Who Am I Page
----------------------

Now we are ready to add a "who am I" page.  First, let's add it to the application, in the constructor for ``TodoListApp``:
```java
        add(WhoAmIPage.class);
```
We'll simply use the URL path ``/who`` for it.  Using ``AuthenticateBasicActeur`` before our ``WhoAmIActeur`` means that the request will fail if the user cannot be authenticated and our subsequent Acteur will never be called;  if it succeeds, there will be a ``User`` object for injection into its constructor.  We also ask for a Jackson ``ObjectMapper`` to be injected, to enable us to write out a JSON response:
```java
    public class WhoAmIPage extends Page {
        private static final String WHO_AM_I_PATTERN = "^who";
        @Inject
        WhoAmIPage(ActeurFactory af) {
            add(af.matchMethods(Method.GET, Method.HEAD));
            add(af.matchPath(WHO_AM_I_PATTERN));
            add(AuthenticateBasicActeur.class);
            add(WhoAmIActeur.class);
            getReponseHeaders().setContentType(MediaType.JSON_UTF_8);
        }

        private static final class WhoAmIActeur extends Acteur {
            @Inject
            WhoAmIActeur(User user, ObjectMapper mapper) throws JsonProcessingException {
                setState(new RespondWith(HttpResponseStatus.OK, mapper.writeValueAsString(user)));
            }
        }
    }
```
We can now run the application and test the new functionality:
```sh
	curl -i --basic --user tim:acteursAreCool http://localhost:8134/who
	HTTP/1.1 200 OK
	Allow: GET, HEAD
	X-Acteur: com.mastfrog.acteur.tutorial.v1.WhoAmIPage$WhoAmIActeur
	X-Page: com.mastfrog.acteur.tutorial.v1.WhoAmIPage
	Content-Length: 75
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 06:25:54 GMT
	X-Req-Path: who

	{"id":"5162507727364e4c50c69d7c","name":"tim","displayName":"Tim Boudreau"}
```
and also make sure it behaves correctly if we give it the wrong password:
```sh
	curl -i --basic --user tim:wrong http://localhost:8134/who
	HTTP/1.1 401 Unauthorized
	Allow: GET, HEAD
	WWW-Authenticate: Basic realm="Users"
	X-Acteur: com.mastfrog.acteur.auth.AuthenticateBasicActeur
	X-Page: com.mastfrog.acteur.tutorial.v1.WhoAmIPage
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 06:25:04 GMT
	X-Req-Path: who
```

The project as described up to this point can be found in the ``acteur-tutorial-v3`` project on GitHub.


Implementing To-Do Lists
========================

We called the application ``TodoListApp``, so it probably should involve a to-do list.  We'll design a trivially simple schema for to-do list items, which can be represented by this pseudo-code:
```json
    {
       _id : ObjectId,
       creator : ObjectId,
       type : 'todo',
       created : long,
       lastModified : long,
       done : boolean,
       title : string,
       tags : [string]
    }
```
The above ought to be fairly self-explanatory - a todo item has a title, a list of tags, ids for itself and the user that created it, dates for creation and last-modification, and can be done or not done. The ``type`` property is there in case we later want to put objects of some other type into the collection, so we query only items that really represent a todo-item.

While there *are* various object-relational type mappers for MongoDB in Java (here is [a good one](https://github.com/mattinsler/guiceymongo)), we aren't really doing something complex enough to justify it.  And in fact, when using a schemaless database, there are benefits to not doing so - in particular, if we don't enforce more than the minimum on the schema, we can have applications that invent their own properties on objects - which can dramatically increase the number of things a web API is good for.

What we will do is map URL parameters into a MongoDB ``BasicDBOBject``, so that that can be used as a query - and we'll do that under-the-hood, so that our acteurs simply see they're getting a ``BasicDBObject``, but don't care about where it comes from.  That will both help to make the code more testable, and mean that we can reuse the same code in any acteur that needs to do this sort of query.

MongoDB lets us query-by-example - we pass in an object which is a partial match (it can also handle [much more complex queries](http://docs.mongodb.org/manual/core/read-operations/) than we are doing here.  Let's add a class which implements Guice's ``Provider`` interface.  We'll bind it so that whenever an Acteur is created which takes a ``BasicDBObject`` in its constructor, this code will supply it:
```java
    final class ListItemsQuery implements Provider<BasicDBObject> {
        private final Provider<Event> eventProvider;
        @Inject
        public ListItemsQuery(Provider<Event> eventProvider) {
            this.eventProvider = eventProvider;
        }
        @Override
        public BasicDBObject get() {
            Event event = eventProvider.get();
            BasicDBObject result = new BasicDBObject();
            for (Map.Entry<String, String> e : event.getParametersAsMap().entrySet()) {
                switch(e.getKey()) {
                    case "_id" :
                    case "creator" :
                        result.put(e.getKey(), new ObjectId(e.getValue()));
                        break;
                    case "created" :
                    case "lastModified" :
                        result.put(e.getKey(), Long.parseLong(e.getValue()));
                        break;
                    case "done" :
                        result.put(e.getKey(), Boolean.parseBoolean(e.getValue()));
                        break;
                    default :
                        result.put(e.getKey(), e.getValue());
                }
            }
            result.put("type", "todo");
            return result;
        }
    }
```
Now we need to bind it using Guice - add this to the nested class ``TodoListModule`` in ``TodoListApp``:
```java
            bind(BasicDBObject.class).toProvider(ListItemsQuery.class);
```
There - that will give us a way to simply ask for a ``BasicDBObject`` query in the constructor, and have it constructed from URL parameters - so our Acteurs get to stay simple and reuse code.

Implementing CRUD for To-Do List Items
--------------------------------------

### Implementing Create

First we will implement a ``CreateItemPage`` which can create new todo-list items: 
```java
    public class CreateItemPage extends Page {
        private static final String ADD_ITEM_PATTERN = "^users/(.*?)/items";
        @Inject
        CreateItemPage(ActeurFactory af) {
            add (af.matchPath(ADD_ITEM_PATTERN));
            add (af.matchMethods(Method.PUT));
```
We will want to ban certain parameters that only the server should set:
```java
            add (af.banParameters("_id", "creator", "lastModified", "created"));
```
And we want to require a ``title`` parameter, since an empty item is not very useful:
```java
            add (af.requireParameters("title"));
```
Do our authentication next-to-last, since we don't want to touch the database until we know we have a valid request:
```java
            add (AuthenticateBasicActeur.class);
            add (AddItemActeur.class);
        }
        private static final class AddItemActeur extends Acteur {
            @Inject
            AddItemActeur(BasicDBObject item, @Named("todo") DBCollection collection, User user, Event evt) {
```
Here we do a little future-proofing - though we won't implement it now, we might want to let users give permission to add items on their behalf.  So we have a URL path element that decides whose items we're modifying.  For now, we'll just respond with FORBIDDEN for that, but we've left the door open to implement it in the future:
```java
                String owner = evt.getPath().getElement(1).toString();
                if (!owner.equals(user.name)) {
                    // For the future
                    setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name 
                            + " cannot add items belonging to " + owner));
                }
```
Here we *could* use ``System.currentTimeMillis()``, but using [Joda Time's](http://joda-time.sourceforge.net/) ``DateTimeUtils`` means we can write tests that expect a specific time to be recorded:
```java
                long now = DateTimeUtils.currentTimeMillis();
                item.put("creator", new ObjectId(user.id));
                item.put("lastModified", now);
                item.put("created", now);
                item.put("done", false);
                collection.save(item);
                setState(new RespondWith(HttpResponseStatus.CREATED, item.toMap()));
            }
        }
    }
```
And we add it to the application in the constructor:
```java
        add(CreateItemPage.class);
```
And we can test it:
```sh
	curl -i -XPUT --basic --user tim:acteursAreCool http://localhost:8134/users/tim/items?title=Try+Acteur
	HTTP/1.1 201 Created
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.CreateItemPage$AddItemActeur
	X-Page: com.mastfrog.acteur.tutorial.v1.CreateItemPage
	Content-Length: 310
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 07:46:17 GMT
	X-Req-Path: users/tim/items

	{"title":"Try Acteur","type":"todo","creator":{"time":1365397623000,"new":false,"inc":1355193724,"machine":657870412,"timeSecond":1365397623},"lastModified":1365407177620,"created":1365407177620,"done":false,"_id":{"time":1365407177000,"new":false,"inc":1547698680,"machine":657892671,"timeSecond":1365407177}}
```

#### Custom JSON for ObjectId

One thing to notice is that the IDs are a little bit ugly.  We are seeing the internal details of MongoDB's ``ObjectId``, which we don't really need - and in fact, ``ObjectId`` has methods to get this information as a simple String - and from the outside, opaque strings are all we need.

The Guice integration which is making the Jackson ``ObjectMapper`` available to us allows us to customize how things are serialized.  Since one might use a multiple JAR files which want to customize how JSON is generated, it uses a classpath-based registration mechanism - the same one used by JDK 6's ``ServiceLoader`` to register objects which configure Jackson, rather than using Guice bindings.  The ``@ServiceProvider`` annotation registers the object:
```java
    @ServiceProvider(service = JacksonConfigurer.class)
    public class JacksonConfiguration implements JacksonConfigurer {
        @Override
        public ObjectMapper configure(ObjectMapper om) {
            om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            SimpleModule sm = new SimpleModule("mongo", new Version(1, 0, 0, null, 
                    "com.mastfrog", "acteur-tutorial"));
            sm.addSerializer(new ObjectMapperSerializer());
            om.registerModule(sm);
            return om;
        }

        private static class ObjectMapperSerializer extends JsonSerializer<ObjectId> {
            @Override
            public Class<ObjectId> handledType() {
                return ObjectId.class;
            }

            @Override
            public void serialize(ObjectId t, JsonGenerator jg, SerializerProvider sp) 
                    throws IOException, JsonProcessingException {
                String id = t.toStringMongod();
                jg.writeString(id);
            }
        }
    }
```
Simply write the above class, clean and build and run again, and we get more palatable output:
```sh
	curl -i -XPUT --basic --user tim:acteursAreCool http://lry+Acteur+Some+More/tim/items?title=Tr
	HTTP/1.1 201 Created
	Allow: PUT
	X-Acteur: com.mastfrog.acteur.tutorial.v1.CreateItemPage$AddItemActeur
	X-Page: com.mastfrog.acteur.tutorial.v1.CreateItemPage
	Content-Length: 182
	Server: TodoListApp
	Date: Mon, 08 Apr 2013 08:07:03 GMT
	X-Req-Path: users/tim/items

	{"title":"Try Acteur Some More","type":"todo","creator":"5162507727364e4c50c69d7c","lastModified":1365408422916,"created":1365408422916,"done":false,"_id":"51627aa6273689cd08407f0e"}
```

### Implementing Read

Now we just want a way to query for todo-list items.  This can be quite simple, since generating the query is already done:
```java
public class SimpleReadItemsPage extends Page {
    @Inject
    SimpleReadItemsPage(ActeurFactory af) {
        add(af.matchPath(CreateItemPage.ITEM_PATTERN));
        add(af.matchMethods(Method.GET));
        add(AuthenticateBasicActeur.class);
        add(FindItemsActeur.class);
    }

    private static final class FindItemsActeur extends Acteur {
        @Inject
        FindItemsActeur(@Named("todo") DBCollection collection, Event evt, User user, BasicDBObject query) {
            String owner = evt.getPath().getElement(1).toString();
            if (!owner.equals(user.name)) {
                // For the future
                setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name
                        + " cannot add items belonging to " + owner));
            }
            DBCursor cursor = collection.find(query).snapshot();
            setState(new RespondWith(HttpResponseStatus.OK, cursor.toArray()));
        }
    }
}
```
Under the hood, we're taking advantage of the fact that the ``RespondWith`` state constructor can take an Object and will convert it to JSON - so it is just getting an array of DbObject and converting it to JSON with the same ``ObjectMapper`` we would use to do it manually.

The downside of doing it this way is that, by converting the cursor to an array, we are loading the entire list of items.  This is harmless if there are relatively few items, but if a few users had 100,000 items and they all made requests at the same time, the result would not be pretty.

The marginally slower but more scalable way to do this is to iteratively output the code.  Doing that involves a little bit of complexity, but it is complexity we only deal with once and the result can be reused again and again.  We will add another Acteur to run after ``FindItemsActeur`` called ``WriteItemsActeur`` - so the constructor of ``ReadItemsPage`` will look like:
```java
    ReadItemsPage(ActeurFactory af) {
        add(af.matchPath(CreateItemPage.ITEM_PATTERN));
        add(af.matchMethods(Method.GET));
        add(AuthenticateBasicActeur.class);
        add(FindItemsActeur.class);
        add(WriteItemsActeur.class);
    }
```
Next we need to make an instance of DbCursor available for injection - modify the annotation on ``TodoListApp`` to look like:
```java
    @ImplicitBindings({User.class, DBCursor.class})
```
Now, in ``ReadItemsPage.FindItemsActeur``, replace the lines after we do the query and get back the ``DBCursor`` to look like:
```java
            if (!cursor.hasNext()) {
                // No items, bail here
                setState(new RespondWith(HttpResponseStatus.OK, "[]\n"));
            } else {
               setState(new ConsumedLockedState(cursor));
            }
```
``ConsumedLockedState`` is a subclass of ``State`` which lets you pass an array of objects that can be injected into the next Acteur.  We use it here to pass the cursor along to our new ``WriteItemsActeur`` which we'll add, nested inside ``ReadItemsPage``.

The end result of the code below is an Acteur which:

  * Gets a cursor injected into its constructor
  * Sets itself as a listener which is called back after the HTTP headers are written to the socket
  * Gets the item under the cursor and writes it out to the socket as JSON
  * Attaches itself as a listener again so it will be called back when what it has just written is sent
  * Finishes the response and closes the connection when the cursor is finished
```java
    static final class WriteItemsActeur extends Acteur implements ChannelFutureListener {

        private final DBCursor cursor;
        private final Event evt;
        private volatile boolean first = true;
        private final ObjectMapper mapper;

        @Inject
        WriteItemsActeur(DBCursor cursor, Event evt, ObjectMapper mapper) {
            this.cursor = cursor;
            this.evt = evt;
            this.mapper = mapper;
            setState(new RespondWith(HttpResponseStatus.OK));
```
The next line tells the framework to attach this as a listener which will be called back once the HTTP headers are sent.
```java
            setResponseBodyWriter(this);
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
```
Since we are iterating once per item, we need to manually write out the leading and trailing []'s of the JSON array:
```java
            if (first) {
                future = future.channel().write(Unpooled.wrappedBuffer(new byte[] { (byte) '[' }));
            }
            if (cursor.hasNext()) {
                DBObject item = cursor.next();
```
This is plain Netty code for writing output:
```java
                future = future.channel().write(Unpooled.copiedBuffer(
                        mapper.writeValueAsString(item.toMap()) 
                        + '\n', CharsetUtil.UTF_8));
```
Cause this listener to be called back again some time after this item has been written to the socket:
```java
                future.addListener(this);
            } else {
                future = future.channel().write(Unpooled.wrappedBuffer(new byte[] { (byte) ']' }));
                if (!evt.isKeepAlive()) {
                    future.addListener(CLOSE);
                }
            }
        }
    }
```
The project as described up to this point can be found in the ``acteur-tutorial-v4`` project on GitHub.

Writing Tests
=============

Any application needs unit tests.  Testing Acteur apps is easy, using a few additional
tools:

 * [Netty HTTP Client](https://github.com/timboudreau/netty-http-client) - an async HTTP client, also
Netty-based, which includes a test-harness subproject with a fluent API for making HTTP calls and
assertions about the results
 * [Giulius Tests](https://github.com/timboudreau/giulius-tests) - a mini-framework/JUnit test runner
which allows us to write test methods that have arguments - those objects are created by Guice
and provided to our test methods

The result is that we can write tests which have almost no set-up code - we just say
"make me a server, start it and pass it to me with a way to call it" and off we go.

Since we need a module which can be instantiated with its default constructor, we'll
make a minor change to ``TodoListApp`` to expose a subclass of ``ServerModule`` instead
of creating one in our ``main()`` method - you can just copy/paste this into
the bottom of ``TodoListApp`` (a future revision of this tutorial will do it this
way from the start):

```java
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerModule<TodoListApp> module = new TodoListModule();
        module.start(port).await();
    }
    
    static class TodoListModule extends ServerModule<TodoListApp> {
        TodoListModule() {
            super(TodoListApp.class);
        }
        @Override
        protected void configure() {
            super.configure();
            bind(Authenticator.class).to(AuthenticatorImpl.class);
            bind(BasicDBObject.class).toProvider(ListItemsQuery.class);
            install(new MongoModule("todo")
                .bindCollection("users", "todoUsers")
                .bindCollection("todo", "todo"));
        }
    }
```

So, our simple test starts with a test file - we're using JUnit 4.11 plus the
test runner.  As you'll see, there is really no boilerplate here:

```java
@RunWith(GuiceRunner.class)
@TestWith({TodoListApp.TodoListModule.class})
public class TodoListAppTest {
    @Test
    public void test(TestHarness harness) {

    }
}
```

Let's add some test code.  First we'll need to create a user we can login with,
so the first call will be to signup.  Since we may want to run the test multiple
times on a running instance of MongoDB, we need a unique user name each time
the test runs.  So we'll append the current time to the user name to make it
unique (the conversion to base 36 saves characters and keeps it in the 
ASCII alphabet):

```java
        String username = "joe" + Long.toString(System.currentTimeMillis(), 36);
        harness.put("users", username, "signup")
                .addQueryPair("displayName", "Joe Blow")
                .setBody("password", PLAIN_TEXT_UTF_8)
                .go()
                .assertStateSeen(StateType.Closed)
                .assertStatus(HttpResponseStatus.OK);
```
That's it - we've signed up, and ensured that we got a 200 OK response.  One
line deserves some explanation:  ``assertStateSeen(StateType.Closed)`` An
HTTP conversation can include more than one response which has a status code
and headers - and in particular, requests where you are sending data usually 
involve sending ``Expect: 100-Continue``.  So this line just makes sure that
the connection is closed before we assert that the status is 200.
 
To flesh things out a little more, we'll create a new todo list item, and then
list all items belonging to our user and assert that it contains the one
we created:

```java
        Map<String,Object> item = (Map<String,Object>) harness
                .put("users", username, "items")
                .addQueryPair("title", "Do stuff")
                .basicAuthentication(username, "password")
                .go()
                .assertStateSeen(StateType.Closed)
                .assertStatus(CREATED)
                .content(Map.class);
        
        Map[] items = (Map[]) harness
                .get("users", username, "items")
                .basicAuthentication(username, "password")
                .addQueryPair("creator", (String) item.get("creator"))
                .go()
                .assertStatus(OK).content(Map[].class);
        
        System.out.println("GOT BACK " + Arrays.asList(items));
        
        assertTrue(Arrays.asList(items).contains(item));
        assertEquals(1, items.length);
```
