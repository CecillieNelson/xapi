package xapi.dev.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import xapi.util.X_Util;

public class StringDataResource extends DelegateClasspathResource{

  public StringDataResource(ClasspathResource source) {
    super(source);
  }

  public Iterable<String> readLines() throws IOException {
    return new Iterable<String>(){
      @Override
      public Iterator<String> iterator() {
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(open()));
          return new StringReader(reader);
        } catch (IOException e) {
          throw X_Util.rethrow(e);
        }
      }
    };
  }

  public String readAll() throws IOException{
    StringBuilder b = new StringBuilder();
    for (String line : readLines())
      b.append(line).append('\n');
    return b.toString();
  }

}
class StringReader implements Iterator<String> {

  private BufferedReader reader;
  private String next;

  public StringReader(BufferedReader reader) {
    this.reader = reader;
  }

  @Override
  public boolean hasNext() {
    try {
      next = reader.readLine();
      if (next == null) {
        reader.close();
        return false;
      }
      return true;
    }catch(IOException e) {
      throw X_Util.rethrow(e);
    }
  }

  @Override
  public String next() {
    return next;
  }

  @Override
  public void remove() {
  }

}