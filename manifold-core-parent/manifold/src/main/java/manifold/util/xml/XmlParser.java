package manifold.util.xml;

import java.io.InputStream;
import java.util.Stack;
import manifold.util.Pair;
import manifold.util.xml.gen.XMLLexer;
import manifold.util.xml.gen.XMLParser;
import manifold.util.xml.gen.XMLParserBaseListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class XmlParser
{
  private XmlElement _root = null;

  public static XmlElement parse( InputStream inputStream )
  {
    return new XmlParser( inputStream )._root;
  }

  public static XMLParser.DocumentContext parseRaw( InputStream inputStream )
  {
    try
    {
      XMLLexer lexer = new XMLLexer( CharStreams.fromStream( inputStream ) );
      CommonTokenStream tokens = new CommonTokenStream( lexer );
      XMLParser parser = new XMLParser( tokens );
      return parser.document();
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  private XmlParser( InputStream inputStream )
  {
    try
    {
      XMLParser.DocumentContext ctx = parseRaw( inputStream );
      ParseTreeWalker walker = new ParseTreeWalker();
      XmlBuilder builder = new XmlBuilder();
      walker.walk( builder, ctx );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  private class XmlBuilder extends XMLParserBaseListener
  {
    private Stack<Pair<XMLParser.ElementContext, XmlElement>> _elements = new Stack<>();
    private XmlAttribute _attribute;

    public void enterElement( XMLParser.ElementContext ctx )
    {
      XmlElement parent = _elements.isEmpty() ? null : _elements.peek().getSecond();
      XmlElement xmlElement = new XmlElement( ctx, parent );
      if( parent != null )
      {
        parent.addChild( xmlElement );
      }
      _elements.push( new Pair<>( ctx, xmlElement ) );
      if( _root == null )
      {
        _root = xmlElement;
      }
    }

    @Override
    public void exitElement( XMLParser.ElementContext ctx )
    {
      Pair<XMLParser.ElementContext, XmlElement> popped = _elements.pop();
      if( popped.getFirst() != ctx )
      {
        throw new IllegalStateException( "Unbalanced elements, expecting '" + ctx.Name( 0 ) +
                                         "' but found '" + popped.getFirst().Name( 0 ) + "'" );
      }
    }

    @Override
    public void enterAttribute( XMLParser.AttributeContext ctx )
    {
      if( _attribute != null )
      {
        throw new IllegalStateException( "Error processing attribute '" + ctx.Name().getText() + "'," +
                                         " there is already an attribute processing: '" + _attribute.getName().getText() );
      }
      XmlElement parent = _elements.peek().getSecond();
      _attribute = new XmlAttribute( ctx, parent );
      parent.addAttribute( _attribute );
    }

    @Override
    public void exitAttribute( XMLParser.AttributeContext ctx )
    {
      if( _attribute == null )
      {
        throw new IllegalStateException( "Expecting non-null attribute during exitAttribute()" );
      }
      _attribute = null;
    }

    @Override
    public void visitTerminal( TerminalNode node )
    {
      Token symbol = node.getSymbol();
      if( _attribute != null )
      {
        if( symbol.getType() == XMLLexer.STRING )
        {
          _attribute.setValue( new XmlTerminal( symbol, _attribute ) );
        }
      }
      else if( !_elements.isEmpty() )
      {
        if( symbol.getType() == XMLLexer.TEXT ||
            symbol.getType() == XMLLexer.STRING ||
            symbol.getType() == XMLLexer.CDATA )
        {
          XmlElement second = _elements.peek().getSecond();
          second.setContent( new XmlTerminal( symbol, second ) );
        }
      }
    }
  }
}