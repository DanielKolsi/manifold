package manifold.science;

import manifold.science.api.AbstractMeasure;
import manifold.science.util.Rational;

public final class Charge extends AbstractMeasure<ChargeUnit, Charge>
{
  public Charge( Rational value, ChargeUnit unit, ChargeUnit displayUnit )
  {
    super( value, unit, displayUnit );
  }

  public Charge( Rational value, ChargeUnit unit )
  {
    this( value, unit, unit );
  }

  @Override
  public ChargeUnit getBaseUnit()
  {
    return ChargeUnit.BASE;
  }

  @Override
  public Charge make( Rational value, ChargeUnit unit, ChargeUnit displayUnit )
  {
    return new Charge( value, unit, displayUnit );
  }

  @Override
  public Charge make( Rational value, ChargeUnit unit )
  {
    return new Charge( value, unit );
  }

  public Current div( Time time )
  {
    return new Current( toBaseNumber() / time.toBaseNumber(), CurrentUnit.BASE, CurrentUnit.get( getDisplayUnit(), time.getDisplayUnit() ) );
  }

  public Time div( Current i )
  {
    return new Time( toBaseNumber() / i.toBaseNumber(), TimeUnit.BASE, i.getDisplayUnit().getTimeUnit() );
  }

  public Capacitance div( Potential p )
  {
    return new Capacitance( toBaseNumber() / p.toBaseNumber(), CapacitanceUnit.BASE, CapacitanceUnit.get( getDisplayUnit(), p.getDisplayUnit() ) );
  }

  public Potential div( Capacitance cap )
  {
    return new Potential( toBaseNumber() / cap.toBaseNumber(), PotentialUnit.BASE, cap.getDisplayUnit().getPotentialUnit() );
  }
}